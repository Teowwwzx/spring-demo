package com.example.springdemo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "paint_requests")
public class PaintRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String requestId; // Unique ID for idempotency

    private String username;
    private String result; // SUCCESS or FAIL

    @Column(name = "created_at")
    private Long createdAt;
}
