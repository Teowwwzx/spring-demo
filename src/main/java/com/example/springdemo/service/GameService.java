package com.example.springdemo.service;

import com.example.springdemo.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

@Service
public class GameService {
    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 初始化数据（用于测试或演示环境）
     * 如果需要初始化数据，可以通过 API 端点手动触发
     */
    public void initData() {
        try {
            // 这里可以添加画布初始化逻辑或测试账号初始化
            System.out.println("✅ 系统数据初始化逻辑已执行");
        } catch (Exception e) {
            System.err.println("⚠️ 初始化失败: " + e.getMessage());
        }
    }
}
