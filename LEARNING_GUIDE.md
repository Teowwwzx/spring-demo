# 🎮 Pixel Global Arena - Technical Learning Guide

This document outlines the enterprise-grade architecture and design patterns implemented in the Pixel Global Arena project. It is designed to demonstrate skills required for high-concurrency and fintech-grade systems.

---

## 1. Core Architecture Overview (核心架构概览)
The system follows a modern Spring Boot **Layered Architecture**, ensuring separation of concerns and scalability.
系统采用现代 Spring Boot **分层架构**，确保代码职责分离及水平扩展能力。

*   **`PixelController.java` / `CanvasController.java`**: REST endpoints for painting and canvas data.
    *   REST 控制器，负责处理绘图请求与画布数据分发。
*   **`PixelPaintService.java`**: The core "Orchestrator" for business logic.
    *   核心“编排服务”，负责整合体力校验、绘图策略执行及缓存同步。
*   **`PlayerRepository.java` / `PixelRepository.java`**: Data access layer with optimized queries.
    *   数据访问层，包含针对高并发优化的自定义 SQL 查询。
*   **`WebSocketConfig.java`**: The real-time communication hub.
    *   实时通信配置中心，负责开启 STOMP 协议支持。

---

## 2. High-Concurrency & Reliability (高并发与金融级可靠性)
We utilize several strategies to handle thousands of users painting simultaneously, meeting Fintech-grade requirements.
针对万人同时涂色的场景，我们采用了金融科技（Fintech）级别的技术手段。

*   **Atomic State Updates (原子化状态更新)**: 
    *   *Implementation:* SQL `UPDATE ... SET energy = energy - :cost ... WHERE energy >= :cost`.
    *   **[中文注释]**: 相比于在 Java 代码中加锁，SQL 原子更新直接在数据库层保证了扣减的原子性，彻底杜绝了体力被扣成负数的“超卖”现象。
*   **Optimistic Locking (乐观锁)**:
    *   *Implementation:* `@Version` annotation in entities.
    *   **[中文注释]**: 假设冲突很少发生，通过版本号控制并发更新。如果两个用户同时修改同一个像素，后提交的会失败，从而保证数据一致性。
*   **Idempotency Handling (幂等性处理)**:
    *   *Implementation:* Using `requestId` to track and deduplicate incoming requests.
    *   **[中文注释]**: 确保由于网络抖动导致的重复请求不会被多次处理。对于扣费、扣体力等敏感操作，这是金融系统的“生命线”。

---

## 3. High-Performance Design (高性能设计)
Optimized for low latency and high throughput.
针对低延迟和高吞吐量进行了专门优化。

*   **Redis Caching (分布式缓存)**:
    *   *Implementation:* Live canvas data is stored in Redis for $O(1)$ lookup speed.
    *   **[中文注释]**: 极大减轻了数据库的读取压力，确保前端获取画布区域数据时能实现毫秒级响应。
*   **Asynchronous Execution (异步执行)**:
    *   *Implementation:* Custom `ThreadPoolTaskExecutor` for non-blocking background tasks.
    *   **[中文注释]**: 比如“成就检查”或“日志记录”可以异步处理，不阻塞用户的绘图主流程，提升系统吞吐量。
*   **ThreadLocal Context (线程上下文持有者)**:
    *   *Implementation:* `UserContextHolder.java` using `ThreadLocal<String>`.
    *   **[中文注释]**: 使用 `ThreadLocal` 存储当前请求的用户 ID，避免了在 Service 层层传递参数。这是实现分布式追踪和统一鉴权的常用模式。

---

## 4. Real-Time Experience (实时交互体验)
*   **WebSockets + STOMP (`WebSocketConfig.java`)**:
    *   *Mechanism:* A persistent "Push" connection instead of "Pull" (polling).
    *   **[中文注释]**: 只要有一个用户涂色，服务器会立即将像素变更通过 WebSocket 推送给所有在线玩家，实现真正的“全球同步”。

---

## 5. Clean Code & Design Patterns (整洁代码与设计模式)
*   **Strategy Pattern (策略模式) (`PaintStrategy.java`)**:
    *   *Used for:* Different tools (Brush, Bomb, Bucket).
    *   **[中文注释]**: 符合“开闭原则”。如果未来要增加新工具，只需新增一个策略类，无需修改原有的 Service 逻辑。
*   **Factory Pattern (工厂模式) (`AchievementFactory.java`)**:
    *   **[中文注释]**: 负责解耦复杂对象（如各类成就勋章）的创建过程，使代码结构更清晰。
*   **Volatile Variables (`GlobalEventManager.java`)**:
    *   **[中文注释]**: 保证全局开关（如“双倍体力活动”）在多线程环境下具有可见性，确保管理员开启活动后，所有服务器线程能立即可见。

---

## 6. Technology Stack Summary (技术栈总结)

| Category | Technology | 中文释义 |
| :--- | :--- | :--- |
| **Framework** | Spring Boot 3+ (Java 21) | 行业标准微服务框架 |
| **Persistence** | Spring Data JPA + H2/PostgreSQL | 强一致性持久化存储 |
| **Caching** | Redis (Distributed) | 应对高并发访问的利器 |
| **Real-time** | Spring WebSocket (STOMP) | 实时双向推送协议 |
| **Concurrency** | SQL Atomic, Optimistic Locking | 保证金融级数据正确性 |
| **Frontend** | HTML5 Canvas + STOMP.js | 高性能画布渲染与实时监听 |
