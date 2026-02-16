package com.example.springdemo.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 本地缓存配置（使用 Caffeine）
 * 用于解决 Redis 热点 Key 问题
 *
 * 场景：当全服玩家都在同一秒去涂中心坐标 (500, 500) 时，
 * 会形成 Hot Key，单个 Redis 节点会产生性能瓶颈。
 *
 * 解决方案：使用 Caffeine 在 JVM 内存中做二级缓存，
 * 每个服务器先在本地汇总请求，然后批量发给 Redis。
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("pixels", "players");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(10000) // 最多缓存 10000 个像素
                .expireAfterWrite(100, TimeUnit.MILLISECONDS) // 100ms 后过期
                .recordStats()); // 记录统计信息
        return cacheManager;
    }
}
