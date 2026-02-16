package com.example.springdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数：同时处理的最小线程数
        executor.setCorePoolSize(20);
        // 最大线程数：高峰期最多能开多少线程
        executor.setMaxPoolSize(100);
        // 队列容量：任务排队的缓冲区
        executor.setQueueCapacity(500);
        // 线程名称前缀，方便日志调试
        executor.setThreadNamePrefix("PixelArena-");
        // 初始化
        executor.initialize();
        return executor;
    }
}
