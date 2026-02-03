Multithreaded Proxy Server with LRU Cache
📌 Overview
This is a Java-based multithreaded proxy server that efficiently handles HTTP GET requests.
It includes:

LRU caching to speed up repeated requests
Rate limiting to prevent excessive traffic
Cache statistics tracking for performance monitoring
⚡ Features
✅ Multithreaded Request Handling
Uses ExecutorService to process multiple client connections concurrently.
Supports GET requests.
✅ LRU Caching for Faster Responses
Implements an LRU (Least Recently Used) cache to store frequently accessed responses.
Reduces repeated network calls by serving cached content.
✅ Cache Statistics Tracking
Tracks:
Cache Hits (Requests served from cache)
Cache Misses (Requests fetched from the origin server)
Cache Evictions (Entries removed due to capacity limits)
✅ Rate Limiting (Token Bucket Algorithm)
Limits the number of requests per client within a given time window.
Prevents excessive traffic and server overload.
