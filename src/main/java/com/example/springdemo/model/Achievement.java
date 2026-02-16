package com.example.springdemo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "achievements")
public class Achievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(name = "achievement_type")
    private String achievementType; // FIRST_PAINT, HUNDRED_PIXELS, THOUSAND_PIXELS, etc.

    private String title;
    private String description;
    private String icon;

    @Column(name = "earned_at")
    private Long earnedAt;
}
