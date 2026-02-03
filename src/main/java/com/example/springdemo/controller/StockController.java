package com.example.springdemo.controller;

import com.example.springdemo.service.OrderService;
import com.example.springdemo.service.RedisOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StockController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisOrderService redisService;

    @GetMapping("/buy")
    public String buy(@RequestParam Long stockId) {
        return orderService.buy(stockId);
    }

    // 映射 URL: http://localhost:4000/stock?stockId=1
    @GetMapping("/stock")
    public int getStock(@RequestParam Long stockId) {
        return orderService.getStock(stockId);
    }

    @GetMapping("/buy-redis")
    public String buyRedis() {
        return redisService.buy();
    }
}
