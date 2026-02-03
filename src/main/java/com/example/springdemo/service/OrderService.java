package com.example.springdemo.service;

import com.example.springdemo.model.Stock;
import com.example.springdemo.repository.StockRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired
    private StockRepository stockRepository;

    @PostConstruct
    public void initData() {
        stockRepository.save(new Stock(null, "Apple", 100, null));
    }

    int count = 0;
    public String buy(Long stockId) {
        Stock stock = stockRepository.findById(stockId).orElseThrow();

        if (stock.getQuantity() > 0) {

            stock.setQuantity(stock.getQuantity() - 1);

            try {
                Thread.sleep(5);
                stockRepository.save(stock);
                count++;
                System.out.println("Person " + count + " bought an apple");
                return "SUCCESS";

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "SYSTEM_ERROR";
            } catch (ObjectOptimisticLockingFailureException e) {
                // This is where the "1,000 people" conflict is caught
                System.out.println("Conflict detected! Someone else bought it first.");
                return "RETRY_OR_FAIL";
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
                return "FAIL";
            }
        } else {
            System.out.println("Out of stock");
            return "FAIL";
        }
    }

    public int getStock(long stockId){
        return stockRepository.findById(stockId).get().getQuantity();
    }
}
