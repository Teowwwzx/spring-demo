package com.example.springdemo.repository;

import com.example.springdemo.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
}
