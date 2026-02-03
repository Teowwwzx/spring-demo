package com.example.springdemo.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

@Service
public class RedisOrderService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    // 商品 Key
    private static final String PRODUCT_KEY = "stock:apple:1";

    @PostConstruct
    public void initData() {
        // 1. 缓存预热：应用启动时，把数据库的库存加载到 Redis
        // 这里为了演示，直接写死 100
        redisTemplate.opsForValue().set(PRODUCT_KEY, "100");
    }

    public String buy() {
        // 2. 原子递减 (Atomic Decrement)
        // 这一步等同于 Redis 命令: DECR stock:apple:1
        // 返回值是扣减后的剩余库存
        Long stockLeft = redisTemplate.opsForValue().decrement(PRODUCT_KEY);

        // 3. 判断结果
        if (stockLeft >= 0) {
            // 抢购成功
            // TODO: 这里通常会发一个消息给 MQ，让消费者慢慢去扣真实的数据库
            System.out.println("抢到了！Redis剩余: " + stockLeft);
            return "SUCCESS";
        } else {
            // 抢购失败 (库存已经是 -1, -2 了)
            // 只是 Redis 里的数字变成负数，不影响数据库，而且计算极快
            return "FAIL";
        }
    }
}
