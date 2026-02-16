package com.example.springdemo.service;

import com.example.springdemo.model.Stock;
import com.example.springdemo.repository.StockRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DatabaseService {
    @Autowired
    private StockRepository stockRepository;

    // ✨ 关键点：@Async 告诉 Spring，这个方法要在独立的线程里跑
    // 不要阻塞调用者的线程！
    @Async
    @Transactional
    public void syncStockToDatabase() {
        try {
            // 模拟慢速 IO
            Thread.sleep(50);

            // ❌ 删掉原来的 读-改-写 逻辑

            // ✅ 换成原子更新
            // 这里不管有多少个线程同时跑，数据库都会让它们排队一个个减
            stockRepository.decreaseStock(1L);

            System.out.println("--> [数据库] 异步扣减 1 个库存");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
