package com.example.springdemo.service;

import com.example.springdemo.model.Stock;
import com.example.springdemo.repository.StockRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired
    private StockRepository stockRepository;

    @PostConstruct
    public void initData() {
        stockRepository.save(new Stock(null, "Apple", 100));
    }

    @Transactional
    public String buy(Long stockId){
        Stock stock = stockRepository.findByIdWithLock(stockId).orElseThrow();

        if (stock.getQuantity() > 0) {
            try { Thread.sleep(5); } catch (InterruptedException e) {}

            stock.setQuantity(stock.getQuantity() - 1);

            stockRepository.save(stock);

            System.out.println( "Bought an apple");
            return "SUCCESS";
        } else {
            System.out.println( "Out of stock");
            return "FAIL";
        }
    }

    public int getStock(long stockId){
        return stockRepository.findById(stockId).get().getQuantity();
    }
}
