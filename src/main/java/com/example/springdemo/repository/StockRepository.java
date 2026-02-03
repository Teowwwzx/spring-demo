package com.example.springdemo.repository;

import com.example.springdemo.model.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
}
