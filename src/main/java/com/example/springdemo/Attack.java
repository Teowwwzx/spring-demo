package com.example.springdemo; // 你的包名

import org.springframework.web.client.RestTemplate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Attack {
    public static void main(String[] args) throws InterruptedException {
        // 1. 准备 1000 个“抢购者” (模拟并发线程)
        int peopleCount = 500;
        ExecutorService threadPool = Executors.newFixedThreadPool(peopleCount);

        // 工具：RestTemplate 用来发 HTTP 请求
        RestTemplate restTemplate = new RestTemplate();

        // 工具：CountDownLatch 就像“发令枪”
        // 我们不希望线程一个个跑，而是希望大家准备好，一声令下同时跑
        CountDownLatch readySignal = new CountDownLatch(peopleCount);

        System.out.println("🔥 正在集结 1000 名抢购者...");

        for (int i = 0; i < peopleCount; i++) {
            threadPool.execute(() -> {
                // 模拟请求：http://localhost:8080/buy?stockId=1
                // 这里的 URL 要和你 Controller 定义的一样
                String url = "http://localhost:4000/buy?stockId=1";

                try {
                    // 发送请求
                    String response = restTemplate.getForObject(url, String.class);
                    // 打印每个人的抢购结果 (Success/Fail)
                    // System.out.println(response);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // 跑完一个，计数器减一
                    readySignal.countDown();
                }
            });
        }

        // 等待所有人都抢完
        readySignal.await();
        threadPool.shutdown();

        System.out.println("✅ 攻击结束！快去 H2 Console 看看库存剩多少！");
    }
}