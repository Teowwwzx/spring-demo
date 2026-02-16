package com.example.springdemo.service;

import com.example.springdemo.model.Stock;
import com.example.springdemo.repository.StockRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

@Service
public class RedisOrderService {
    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private DatabaseService databaseService;

    // @PostConstruct - 注释掉以避免启动时必须连接 Redis
    // 如果需要初始化数据，可以通过 API 端点手动触发
    public void initData() {
        try {
            stockRepository.save(new Stock(null, "Apple", 100));
            // 1. 缓存预热：应用启动时，把数据库的库存加载到 Redis
            // 这里为了演示，直接写死 100
            redisTemplate.opsForValue().set(PRODUCT_KEY, "100");
            System.out.println("✅ Redis 初始化成功");
        } catch (Exception e) {
            System.err.println("⚠️ Redis 连接失败，但应用继续运行: " + e.getMessage());
        }
    }

    // 商品 Key
    private static final String PRODUCT_KEY = "stock:apple:1";

    public String buy() {
        // 1. Redis 极速扣减
        Long stockLeft = redisTemplate.opsForValue().decrement(PRODUCT_KEY);

        if (stockLeft >= 0) {
            // 2. 抢到了！

            // 3. 【关键】触发异步同步
            // 这行代码会立刻返回，不会等待 50ms 的数据库操作
            databaseService.syncStockToDatabase();

            System.out.println("抢到了！Redis剩余: " + stockLeft);
            return "SUCCESS";
        } else {
            // 抢购失败 (库存已经是 -1, -2 了)
            // 只是 Redis 里的数字变成负数，不影响数据库，而且计算极快
            return "FAIL";
        }
    }
}
