package com.example.springdemo.controller;

import com.example.springdemo.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统管理控制器
 */
@RestController
@RequestMapping("/api/system")
public class SystemController {

    @Autowired
    private GameService gameService;

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("application", "Pixel Global Arena");
        status.put("timestamp", System.currentTimeMillis());
        return status;
    }

    /**
     * 手动初始化 Redis 数据
     */
    @PostMapping("/init-redis")
    public String initRedis() {
        try {
            gameService.initData();
            return "✅ Redis 初始化成功";
        } catch (Exception e) {
            return "❌ Redis 初始化失败: " + e.getMessage();
        }
    }

    /**
     * 系统信息
     */
    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("application", "Pixel Global Arena");
        info.put("version", "1.0.0");
        info.put("description", "A high-concurrency pixel painting game backend");
        info.put("features", new String[]{
                "1000x1000 Pixel Canvas",
                "Player Energy System",
                "3 Paint Tools (Normal Brush, Bomb, Paint Bucket with BFS)",
                "Achievement System",
                "Redis Caching",
                "Idempotency Support",
                "ThreadLocal User Context",
                "Volatile Global Events"
        });
        return info;
    }
}
