# Pixel Global Arena - API Guide

## 🎮 项目概述

这是一个基于 Spring Boot 的像素大战游戏后端系统，实现了：
- 1000x1000 像素画布
- 玩家体力系统
- 多种绘画工具（策略模式）
- 成就系统（工厂模式）
- Redis 缓存优化
- 并发控制与幂等性保证
- 全局事件管理（volatile）
- ThreadLocal 用户上下文

---

## 🚀 快速开始

### 1. 启动 Redis

```bash
# Windows (使用默认端口 6380)
redis-server --port 6380

# Linux/Mac
redis-server --port 6380
```

### 2. 启动应用

```bash
# 使用 Maven
./mvnw spring-boot:run

# 或者运行打包好的 jar
java -jar target/spring-demo-0.0.1-SNAPSHOT.jar
```

### 3. 运行并发测试

```bash
# 编译并运行 Attack.java
java -cp target/spring-demo-0.0.1-SNAPSHOT.jar com.example.springdemo.Attack
```

---

## 📡 API 接口

### 玩家管理 API

#### 注册玩家
```
POST /api/player/register?username=alice
```

返回：
```json
{
  "id": 1,
  "username": "alice",
  "energy": 100,
  "maxEnergy": 100,
  "pixelsPainted": 0,
  "lastEnergyUpdate": 1234567890,
  "createdAt": 1234567890
}
```

#### 获取玩家信息
```
GET /api/player/info?username=alice
```

返回：
```json
{
  "player": {
    "id": 1,
    "username": "alice",
    "energy": 95,
    "pixelsPainted": 5
  },
  "achievements": [
    {
      "achievementType": "FIRST_PAINT",
      "title": "初次绘画",
      "icon": "🎨"
    }
  ]
}
```

#### 获取玩家成就
```
GET /api/player/achievements?username=alice
```

---

### 像素绘画 API

#### 绘制像素（完整版）
```
POST /api/pixel/paint?username=alice&x=500&y=500&color=%23FF0000&tool=normalBrush&requestId=unique-id-123
```

参数说明：
- `username`: 玩家用户名（必需）
- `x`: X坐标 (0-999)
- `y`: Y坐标 (0-999)
- `color`: 颜色（十六进制，如 #FF0000 表示红色）
- `tool`: 工具类型
  - `normalBrush` - 普通刷子（1个像素，消耗1点体力）
  - `bomb` - 炸弹（3x3区域，消耗5点体力）
  - `paintBucket` - 油漆桶（BFS填充连通区域，消耗10点体力）
- `requestId`: 请求唯一ID（可选，用于幂等性保证）

返回：
```
SUCCESS: 使用 普通刷子 绘制了 1 个像素
```

#### 绘制像素（简化版）
```
GET /api/pixel/paint-simple?username=alice&x=500&y=500&color=%23FF0000
```

---

### 画布查询 API

#### 获取指定区域的像素
```
GET /api/canvas/region?xStart=490&xEnd=510&yStart=490&yEnd=510
```

返回：
```json
[
  {
    "x": 500,
    "y": 500,
    "color": "#FF0000",
    "paintedBy": "alice"
  },
  {
    "x": 501,
    "y": 500,
    "color": "#00FF00",
    "paintedBy": "bob"
  }
]
```

#### 获取单个像素信息
```
GET /api/canvas/pixel?x=500&y=500
```

返回：
```json
{
  "x": 500,
  "y": 500,
  "color": "#FF0000",
  "paintedBy": "alice",
  "paintedAt": 1234567890,
  "source": "redis"
}
```

#### 获取画布统计
```
GET /api/canvas/stats
```

返回：
```json
{
  "totalPixelsPainted": 12345,
  "canvasSize": "1000x1000",
  "totalPixelsAvailable": 1000000,
  "percentagePainted": 1.2345
}
```

---

### 全局事件管理 API（管理员功能）

#### 开启双倍体力活动
```
POST /api/event/double-energy/enable
```

#### 关闭双倍体力活动
```
POST /api/event/double-energy/disable
```

#### 开启免费绘画活动
```
POST /api/event/free-draw/enable
```

#### 关闭免费绘画活动
```
POST /api/event/free-draw/disable
```

#### 设置自定义公告
```
POST /api/event/announcement?message=欢迎来到像素大战！
```

#### 获取当前活动状态
```
GET /api/event/status
```

返回：
```json
{
  "doubleEnergyEvent": false,
  "freeDrawEvent": true,
  "announcement": "🎨 免费绘画活动已开启！不消耗体力！"
}
```

---

## 🎨 绘画工具说明

### 1. 普通刷子 (normalBrush)
- **消耗体力**: 1 点
- **效果**: 涂色单个像素点
- **适用场景**: 精细绘画

### 2. 炸弹 (bomb)
- **消耗体力**: 5 点
- **效果**: 以指定坐标为中心，涂色 3x3 区域（9个像素）
- **适用场景**: 快速占领区域

### 3. 油漆桶 (paintBucket)
- **消耗体力**: 10 点
- **效果**: 使用 BFS 算法填充连通的同色区域
- **限制**: 最多填充 100 个像素（防止恶意刷屏）
- **适用场景**: 大面积填充

---

## 🏆 成就系统

玩家在游戏过程中可以获得以下成就：

| 成就类型 | 图标 | 标题 | 描述 | 获得条件 |
|---------|------|------|------|---------|
| FIRST_PAINT | 🎨 | 初次绘画 | 完成你的第一个像素点涂色 | 绘制第1个像素 |
| HUNDRED_PIXELS | 🖌️ | 百像素画师 | 成功绘制 100 个像素点 | 绘制第100个像素 |
| THOUSAND_PIXELS | 🏆 | 千像素大师 | 成功绘制 1000 个像素点 | 绘制第1000个像素 |
| TERRITORY_MASTER | 👑 | 领地霸主 | 占领一片 10x10 的完整区域 | 待实现 |
| SPEED_PAINTER | ⚡ | 闪电画师 | 在 1 秒内完成 10 次涂色 | 待实现 |
| RAINBOW_ARTIST | 🌈 | 彩虹艺术家 | 使用 7 种不同颜色完成涂色 | 待实现 |

---

## 🔧 技术亮点

### 1. ThreadLocal 用户上下文
```java
// 在 Controller 中设置用户
UserContextHolder.setUser(username);

// 在 Service 层直接获取，无需传参
String currentUser = UserContextHolder.getUser();
```

### 2. Volatile 全局事件
```java
// 管理员开启活动，所有线程立即可见
globalEventManager.enableFreeDrawEvent();

// 所有玩家的请求都会检查到这个状态
if (globalEventManager.isFreeDrawEvent()) {
    // 免费绘画，不消耗体力
}
```

### 3. 幂等性保证
```java
// 使用 requestId 防止重复请求
String requestId = UUID.randomUUID().toString();
pixelPaintService.paintPixel(requestId, x, y, color, tool);

// 重复提交相同 requestId 会返回第一次的结果
```

### 4. 策略模式
```java
// 动态选择绘画策略
PaintStrategy strategy = paintStrategyMap.get(toolType);
List<int[]> affectedPixels = strategy.paint(x, y, color, username);
```

### 5. 工厂模式
```java
// 使用工厂创建成就对象
Achievement achievement = AchievementFactory.createAchievement(
    AchievementFactory.AchievementType.FIRST_PAINT,
    username
);
```

### 6. 二级缓存（Redis + Caffeine）
- **Redis**: 分布式缓存，所有服务器共享
- **Caffeine**: 本地 JVM 缓存，100ms 过期，解决热点 Key 问题

---

## 📊 数据库访问

### H2 控制台
访问: http://localhost:4000/h2-console

连接信息：
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `admin`
- Password: `123123`

### 数据表说明

- `players` - 玩家信息表
- `pixels` - 像素数据表（带 x, y 复合索引）
- `paint_requests` - 绘画请求表（幂等性）
- `achievements` - 成就表
- `stock` - 旧的库存表（保留用于对比）

---

## 🧪 并发测试

运行 `Attack.java` 会模拟 100 个玩家同时争夺画布中心区域：

```bash
🎨 像素大战开始！100 名玩家正在抢夺画布中心...
✅ player0 绘制成功！SUCCESS: 使用 普通刷子 绘制了 1 个像素
✅ player1 绘制成功！SUCCESS: 使用 炸弹 (3x3) 绘制了 9 个像素
❌ player2 绘制失败：FAIL: 体力不足 (需要 10 点体力)

============================================================
🏁 像素大战结束！
============================================================
总玩家数: 100
成功绘制: 85 (85.0%)
绘制失败: 15 (15.0%)
总耗时: 2345 ms
平均响应时间: 23.45 ms/请求
QPS: 42.64 请求/秒
============================================================
```

---

## 💡 使用示例

### 示例 1: 基础绘画流程

```bash
# 1. 注册玩家
curl -X POST "http://localhost:4000/api/player/register?username=alice"

# 2. 使用普通刷子绘制像素
curl -X POST "http://localhost:4000/api/pixel/paint?username=alice&x=500&y=500&color=%23FF0000&tool=normalBrush"

# 3. 查看玩家信息
curl "http://localhost:4000/api/player/info?username=alice"

# 4. 查看画布统计
curl "http://localhost:4000/api/canvas/stats"
```

### 示例 2: 开启活动

```bash
# 管理员开启免费绘画活动
curl -X POST "http://localhost:4000/api/event/free-draw/enable"

# 玩家绘画不消耗体力
curl -X POST "http://localhost:4000/api/pixel/paint?username=alice&x=501&y=500&color=%2300FF00&tool=bomb"

# 关闭活动
curl -X POST "http://localhost:4000/api/event/free-draw/disable"
```

### 示例 3: 使用油漆桶填充

```bash
# 使用油漆桶在连通区域填充
curl -X POST "http://localhost:4000/api/pixel/paint?username=bob&x=100&y=100&color=%230000FF&tool=paintBucket"
```

---

## 🐛 故障排查

### Redis 连接失败
```
错误: Connection refused: localhost:6380
解决: 确保 Redis 已启动在 6380 端口
```

### 体力不足
```
错误: FAIL: 体力不足 (需要 10 点体力)
解决: 等待体力恢复（每秒恢复1点），或开启免费绘画活动
```

### 幂等性重复请求
```
警告: ⚠️ 重复请求，requestId: xxx 已处理过
说明: 这是正常现象，系统检测到重复请求并返回了第一次的结果
```

---

## 📈 性能优化建议

1. **启用 Redis** - 大幅提升热点像素的访问速度
2. **调整线程池大小** - 根据实际 QPS 调整 `AsyncConfig` 中的线程池参数
3. **数据库索引** - 已在 `pixels` 表的 (x, y) 上建立复合索引
4. **二级缓存** - Caffeine 本地缓存可以减少对 Redis 的压力

---

## 📚 相关链接

- [Spring Boot 文档](https://spring.io/projects/spring-boot)
- [Redis 文档](https://redis.io/documentation)
- [Caffeine 缓存](https://github.com/ben-manes/caffeine)
- [H2 数据库](https://www.h2database.com/)

---

**祝你在像素大战中玩得开心！🎨🎮**
