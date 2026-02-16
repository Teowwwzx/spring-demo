package com.example.springdemo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "players")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private Integer energy; // Current energy, max 100

    @Column(name = "max_energy")
    private Integer maxEnergy; // Maximum energy capacity

    @Column(name = "pixels_painted")
    private Integer pixelsPainted; // Total pixels painted by this player

    @Column(name = "last_energy_update")
    private Long lastEnergyUpdate; // Timestamp for energy regeneration

    @Column(name = "created_at")
    private Long createdAt;

    @Version
    private Long version; // For optimistic locking
}
