# 🎮 Pixel Global Arena - Technical Learning Guide

This document outlines the enterprise-grade architecture and design patterns implemented in the Pixel Global Arena project. It is designed to demonstrate skills required for high-concurrency and fintech-grade systems.

---

## 1. Core Architecture Overview (核心架构概览)
The system follows a modern Spring Boot **Layered Architecture**, ensuring separation of concerns and scalability.

*   **`PixelController.java`**: REST endpoints for single and **Batch** painting.
*   **`PixelPaintService.java`**: The "Orchestrator" managing idempotency, energy, and caching.
*   **`PlayerRepository.java`**: Data access with **Atomic SQL Updates** to prevent concurrency bugs.
*   **`WebSocketConfig.java`**: Real-time STOMP hub for global state synchronization.

---

## 2. High-Concurrency & Reliability (高并发与金融级可靠性)
*   **Atomic State Updates (原子化状态更新)**: 
    *   *Implementation:* `UPDATE Player p SET p.energy = p.energy - :cost WHERE p.energy >= :cost`.
    *   **[中文]**: 放弃了传统的 `save()`，改用原生 SQL 原子更新。这在高并发环境下直接利用数据库行锁，彻底杜绝了 `StaleStateException` 和“超卖”现象。
*   **Idempotency Handling (幂等性处理)**:
    *   *Implementation:* `requestId` tracking in `PaintRequestRepository`.
    *   **[中文]**: 确保支付或扣减类操作在网络重试时不会发生“二次扣费”，这是金融级系统的核心防线。

---

## 3. High-Performance Design (高性能设计)
*   **API Batching (接口批处理)**:
    *   *Implementation:* `/api/pixel/paint-batch` endpoint.
    *   **[中文]**: 将数百个像素点合并为一个请求发送。极大减少了网络 RTT（往返时间）和服务器 Context Switch（上下文切换）开销。
*   **Redis Caching (分布式缓存)**:
    *   *Implementation:* Canvas data stored in Redis for $O(1)$ speed.
*   **ThreadLocal Context**: Used in `UserContextHolder` to avoid parameter drilling.

---

## 4. Frontend Engineering & UX (前端工程化与用户体验)
*   **p5.js Framework**:
    *   *Why:* Used for professional graphics rendering and sub-pixel smoothing.
    *   **[中文]**: 使用 p5.js 提供的 `line()` 和 `mouseDragged()` 实现了“丝滑”的绘图体验，替代了低效的原始 Canvas 事件监听。
*   **Line Interpolation (线条插值)**:
    *   *Algorithm:* **Bresenham's Algorithm**.
    *   **[中文]**: 当用户快速移动鼠标时，前端自动计算并填充轨迹间的空隙，确保线条连续不“断点”。
*   **Optimistic UI (乐观 UI)**:
    *   *Mechanism:* Draw locally first, sync with server later.
    *   **[中文]**: 提供“零延迟”的视觉反馈。在网络请求返回前就完成局部渲染，提升用户留存。

---

## 5. Mock Interview Questions (蚂蚁金服/Antom 模拟面试题)

### Q1: Why did you use Batching for drawing? (为什么使用批量接口？)
> **Answer:** "In high-frequency scenarios like drawing, single-pixel requests create massive overhead (TCP headers, HTTP parsing, DB transactions). Batching reduces N requests to 1, significantly lowering the pressure on the Thread Pool and Database IOPS."

### Q2: How did you solve the 'StaleStateException' during high-speed drawing? (如何解决并发更新冲突？)
> **Answer:** "I moved from Hibernate's optimistic locking (`@Version`) to **Native Atomic Updates**. By using `UPDATE SET energy = energy - 1 ... WHERE energy >= 1`, we push the conflict resolution down to the DB row-level lock, which is much more efficient than a 'fetch-modify-retry' cycle."

### Q3: How do you handle consistency between Redis and the Database? (Redis 和数据库的一致性如何保证？)
> **Answer:** "We use a **Write-Through** style where Redis is updated for real-time reads (WebSockets), while the Database remains the source of truth for energy and state. For production, I would use a **Canal** or **CDC (Change Data Capture)** approach to ensure Redis is always in sync with the DB binlog."

### Q4: If 10,000 users paint the same pixel, how do you handle the 'Hot Key'? (万名玩家涂同一个点，如何处理热点 Key？)
> **Answer:** "For extreme hot keys, I would implement **Local Caching (Caffeine)** on the Java side to aggregate updates for 100ms before hitting Redis, effectively 'merging' 10,000 writes into one."

---

## 6. Technology Stack Summary (技术栈总结)

| Category | Technology | Purpose |
| :--- | :--- | :--- |
| **Backend** | Spring Boot 3+ (Java 21) | Enterprise Microservice |
| **Graphics** | p5.js | Smooth Canvas & UX |
| **Real-time** | WebSocket (STOMP) | Live State Sync |
| **Batching** | JSON List Objects | Network Optimization |
| **Consistency** | Native SQL Atomic | High-Concurrency Safety |
