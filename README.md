# 🚀 Multithreaded Proxy Server with LRU Cache

## 📌 Overview

This is a **Java-based multithreaded HTTP proxy server** designed to efficiently handle multiple client requests concurrently.  
The project focuses on **performance optimization** using **LRU caching**, **thread pooling**, and **request control mechanisms**.

### 🔍 Key Highlights
- Handles multiple client connections simultaneously
- Supports HTTP `GET` requests
- Uses an **LRU (Least Recently Used) cache** to speed up repeated requests
- Tracks cache statistics for performance monitoring
- Implements basic rate limiting to prevent excessive traffic

---

## ⚡ Features

### ✅ Multithreaded Request Handling
- Uses Java’s `ExecutorService` for efficient thread management
- Processes multiple client connections concurrently
- Improves responsiveness under heavy load

---

### ✅ LRU Caching for Faster Responses
- Stores frequently accessed responses in memory
- Automatically evicts least recently used entries
- Reduces latency and external server requests

---

### ✅ Cache Statistics Tracking
- Tracks cache hits and misses
- Helps analyze performance improvements
- Useful for optimization and debugging

---

### ✅ Rate Limiting
- Prevents a single client from overwhelming the server
- Improves stability under high traffic conditions

---

## 🧠 Technologies Used

- **Java**
- **Java Networking (Socket Programming)**
- **Multithreading (ExecutorService)**
- **LRU Cache (Custom Implementation)**
- **HTTP Protocol (GET Requests)**


