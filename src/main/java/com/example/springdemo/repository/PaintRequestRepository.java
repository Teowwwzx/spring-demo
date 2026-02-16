package com.example.springdemo.repository;

import com.example.springdemo.model.PaintRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaintRequestRepository extends JpaRepository<PaintRequest, Long> {
    Optional<PaintRequest> findByRequestId(String requestId);
}
