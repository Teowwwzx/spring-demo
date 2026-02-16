package com.example.springdemo.controller;

import com.example.springdemo.service.RedisOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StockController {

    @Autowired
    private RedisOrderService redisService;

    @GetMapping("/buy-redis")
    public String buyRedis() {
        return redisService.buy();
    }
}
