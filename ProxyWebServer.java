import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

// Node class for the doubly linked list in LRU Cache
class Node {
    String key;
    String value;
    Node prev, next;

    Node(String key, String value) {
        this.key = key;
        this.value = value;
        this.prev = null;
        this.next = null;
    }
}

// LRU Cache class
class LRUCache {
    private final int capacity;
    private final Map<String, Node> cacheMap;
    private Node head, tail;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.cacheMap = new HashMap<>();
        this.head = null;
        this.tail = null;
    }

    public synchronized String get(String key) {
        if (cacheMap.containsKey(key)) {
            Node node = cacheMap.get(key);
            moveToHead(node);
            System.out.println("[CACHE] HIT for: " + key);
            return node.value;
        }
        System.out.println("[CACHE] MISS for: " + key);
        return null;
    }

    public synchronized void put(String key, String value) {
        if (cacheMap.containsKey(key)) {
            Node node = cacheMap.get(key);
            node.value = value;
            moveToHead(node);
            System.out.println("[CACHE] Updated and moved to head: " + key);
        } else {
            Node newNode = new Node(key, value);
            if (cacheMap.size() >= capacity) {
                System.out.println("[CACHE] Capacity full. Removing LRU: " + tail.key);
                removeTail();
            }
            addToHead(newNode);
            cacheMap.put(key, newNode);
            System.out.println("[CACHE] PUT for: " + key);
        }
    }

    private void removeTail() {
        if (tail != null) {
            cacheMap.remove(tail.key);
            if (tail.prev != null) {
                tail.prev.next = null;
            } else {
                head = null;
            }
            tail = tail.prev;
        }
    }

    private void moveToHead(Node node) {
        if (node == head) return;

        if (node.prev != null) node.prev.next = node.next;
        if (node.next != null) node.next.prev = node.prev;

        if (node == tail) tail = node.prev;

        node.next = head;
        node.prev = null;

        if (head != null) head.prev = node;
        head = node;

        if (tail == null) tail = head;
    }

    private void addToHead(Node node) {
        node.next = head;
        node.prev = null;

        if (head != null) head.prev = node;
        head = node;

        if (tail == null) tail = head;
    }
}

// Proxy Server class
class ProxyServer {
    private final int port;
    private final LRUCache cache;
    private final Semaphore semaphore;
    private final ExecutorService threadPool;

    public ProxyServer(int port, int maxClients, int cacheSize) {
        this.port = port;
        this.cache = new LRUCache(cacheSize);
        this.semaphore = new Semaphore(maxClients);
        this.threadPool = Executors.newFixedThreadPool(maxClients);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("🚀 Proxy server is running on port: " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                semaphore.acquire();
                threadPool.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Server error: " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        System.out.println("📥 Received client connection.");
        long startTime = System.currentTimeMillis();

        try (BufferedReader clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedWriter clientWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

            String requestLine = clientReader.readLine();
            if (requestLine == null || !requestLine.startsWith("GET")) {
                sendError(clientWriter, "400 Bad Request");
                return;
            }

            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 2) {
                sendError(clientWriter, "400 Bad Request");
                return;
            }

            String url = requestParts[1];
            if (url.startsWith("//")) {
                url = "http:" + url;
            }

            // Ignore static resources
            if (isStaticResource(url)) {
                return;
            }

            System.out.println("🌐 Processing request for: " + url);
            String cachedResponse = cache.get(url);

            if (cachedResponse != null) {
                sendResponse(clientWriter, cachedResponse);
            } else {
                String remoteResponse = fetchFromRemoteServer(url);
                if (remoteResponse != null) {
                    cache.put(url, remoteResponse);
                    sendResponse(clientWriter, remoteResponse);
                } else {
                    sendError(clientWriter, "404 Not Found");
                }
            }
        } catch (IOException e) {
            System.err.println("⚠️ Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("⚠️ Error closing socket: " + e.getMessage());
            }
            semaphore.release();
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("⏱️ Request handled in " + duration + " ms\n");
        }
    }

    private boolean isStaticResource(String url) {
        return url.endsWith(".ico") || url.endsWith(".png") || url.endsWith(".jpg") ||
               url.endsWith(".css") || url.endsWith(".js") || url.contains("favicon") ||
               url.contains("/images/") || url.contains("/client_204") || url.contains("/xjs/");
    }

    private String fetchFromRemoteServer(String url) {
        try {
            URL remoteUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) remoteUrl.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                System.out.println("❌ Remote server responded with: " + responseCode);
                return null;
            }

            BufferedReader remoteReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;

            while ((line = remoteReader.readLine()) != null) {
                responseBuilder.append(line).append("\n");
            }
            remoteReader.close();

            System.out.println("✅ Fetched from remote: " + url);
            return responseBuilder.toString();
        } catch (IOException e) {
            System.err.println("❌ Error fetching from remote: " + e.getMessage());
            return null;
        }
    }

    private void sendResponse(BufferedWriter writer, String response) throws IOException {
        writer.write("HTTP/1.1 200 OK\r\n");
        writer.write("Content-Length: " + response.length() + "\r\n");
        writer.write("\r\n");
        writer.write(response);
        writer.flush();
    }

    private void sendError(BufferedWriter writer, String errorMessage) throws IOException {
        writer.write("HTTP/1.1 " + errorMessage + "\r\n");
        writer.write("Content-Length: 0\r\n");
        writer.write("\r\n");
        writer.flush();
    }
}

// Main class to run the server
public class ProxyWebServer {
    public static void main(String[] args) {
        int port = 8080;
        int maxClients = 10;
        int cacheSize = 5;

        ProxyServer proxyServer = new ProxyServer(port, maxClients, cacheSize);
        proxyServer.start();
    }
}