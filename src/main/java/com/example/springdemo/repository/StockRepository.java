package com.example.springdemo.repository;

import com.example.springdemo.model.Stock;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    // 🔥 核心解法：原子更新 SQL
    // 数据库在执行这条 UPDATE 语句时，会自动给这一行加写锁 (Row Lock)
    // 保证同一时间只有一个线程能减成功
    @Modifying
    @Transactional
    @Query("UPDATE Stock s SET s.quantity = s.quantity - 1 WHERE s.id = :id AND s.quantity > 0")
    int decreaseStock(Long id);
}
