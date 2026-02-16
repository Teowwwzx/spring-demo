package com.example.springdemo;

import org.springframework.web.client.RestTemplate;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 像素大战并发测试
 * 模拟多个玩家同时争夺画布中心像素的场景
 */
public class Attack {
    public static void main(String[] args) throws InterruptedException {
        // 1. 模拟 100 个玩家并发绘画
        int playerCount = 100;
        ExecutorService threadPool = Executors.newFixedThreadPool(playerCount);

        RestTemplate restTemplate = new RestTemplate();
        CountDownLatch latch = new CountDownLatch(playerCount);

        // 统计成功和失败的数量
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        System.out.println("🎨 像素大战开始！" + playerCount + " 名玩家正在抢夺画布中心...");

        // 热点坐标：画布中心 (500, 500)
        int hotX = 500;
        int hotY = 500;

        Random random = new Random();
        String[] colors = {"#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#FF00FF", "#00FFFF"};

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < playerCount; i++) {
            final int playerId = i;
            threadPool.execute(() -> {
                try {
                    // 每个玩家使用自己的用户名
                    String username = "player" + playerId;

                    // 随机选择颜色
                    String color = colors[random.nextInt(colors.length)];

                    // 随机选择工具：70% 普通刷子，20% 炸弹，10% 油漆桶
                    String tool;
                    int toolChoice = random.nextInt(100);
                    if (toolChoice < 70) {
                        tool = "normalBrush";
                    } else if (toolChoice < 90) {
                        tool = "bomb";
                    } else {
                        tool = "paintBucket";
                    }

                    // 在中心区域附近随机选择坐标 (模拟热点区域)
                    int x = hotX + random.nextInt(10) - 5;
                    int y = hotY + random.nextInt(10) - 5;

                    // 发送绘画请求
                    String url = String.format(
                            "http://localhost:4000/api/pixel/paint?username=%s&x=%d&y=%d&color=%s&tool=%s",
                            username, x, y, color, tool
                    );

                    String response = restTemplate.postForObject(url, null, String.class);

                    // 统计结果
                    if (response != null && response.contains("SUCCESS")) {
                        successCount.incrementAndGet();
                        System.out.println("✅ " + username + " 绘制成功！" + response);
                    } else {
                        failCount.incrementAndGet();
                        System.out.println("❌ " + username + " 绘制失败：" + response);
                    }

                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.err.println("⚠️ 请求异常: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有线程完成
        latch.await();
        threadPool.shutdown();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("\n" + "=".repeat(60));
        System.out.println("🏁 像素大战结束！");
        System.out.println("=".repeat(60));
        System.out.println("总玩家数: " + playerCount);
        System.out.println("成功绘制: " + successCount.get() + " (" + (successCount.get() * 100.0 / playerCount) + "%)");
        System.out.println("绘制失败: " + failCount.get() + " (" + (failCount.get() * 100.0 / playerCount) + "%)");
        System.out.println("总耗时: " + duration + " ms");
        System.out.println("平均响应时间: " + (duration * 1.0 / playerCount) + " ms/请求");
        System.out.println("QPS: " + (playerCount * 1000.0 / duration) + " 请求/秒");
        System.out.println("=".repeat(60));
        System.out.println("\n💡 提示:");
        System.out.println("1. 访问 http://localhost:4000/h2-console 查看数据库");
        System.out.println("2. 访问 http://localhost:4000/api/canvas/stats 查看画布统计");
        System.out.println("3. 访问 http://localhost:4000/api/canvas/pixel?x=500&y=500 查看中心像素");
    }
}