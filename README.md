# Distributed File System (Java RMI)

## Overview

This project implements a **distributed file system** using Java RMI, where multiple nodes cooperate to perform file operations in a consistent and ordered manner.

The system ensures **Total Order Execution** of all operations using:

* Lamport Logical Clocks
* Totally Ordered Multicast
* Acknowledgment (ACK) mechanism

This guarantees that all nodes execute requests in the same order, even when operations occur concurrently.

---

## Author

**Habiba Abouraya**

---

## Features

* Distributed file operations:

  * Upload
  * Delete
  * Search
  * Download
* Totally ordered multicast communication
* Logical clock synchronization (Lamport clocks)
* Consistent execution across all nodes
* Leader-based handling for search and download operations

---

## System Architecture

The system is divided into the following packages:

### common

* `Request` → Represents file operation requests (UPLOAD, DELETE, SEARCH, DOWNLOAD)
* `ACK` → Acknowledgment messages used to confirm receipt of requests

### rmi

* `NodeInterface` → Defines remote methods for communication between nodes

### node

* `Node` → Core system logic:

  * Maintains logical clock
  * Stores requests in a priority queue
  * Tracks acknowledgments
  * Executes requests in correct order

### client

* `StartSystem` → Initializes the system:

  * Starts RMI registry
  * Creates nodes
  * Connects nodes together
  * Runs test scenarios

---

## How It Works

1. A node creates a request (UPLOAD, DELETE, SEARCH, DOWNLOAD)
2. The request is multicast to all nodes
3. Each node:

   * Updates its logical clock
   * Adds the request to a priority queue
   * Sends an ACK to all other nodes
4. A request is executed only when:

   * ACKs are received from all nodes
5. Requests are executed in order based on:

   * Timestamp (Lamport clock)
   * Request ID (tie-breaker)

---

##  Test Scenario

The system demonstrates correctness using concurrent operations:

* Node 1 → Upload file
* Node 2 → Delete file
* Node 3 → Search file
* Node 4 → Download file

All nodes process these operations in the **same order**, ensuring consistency.

---

## ▶️ How to Run

1. Compile the project:

```bash
javac */*.java
```

2. Run the system:

```bash
java client.StartSystem
```

3. Observe the output in the console

---

## 📄 Documentation

See full report here:
[Project Report](docs/report.pdf)

