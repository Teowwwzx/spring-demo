package com.example.springdemo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pixels", indexes = {
    @Index(name = "idx_coordinates", columnList = "x, y")
})
public class Pixel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer x;
    private Integer y;
    private String color; // Hex color code like "#FF0000"

    @Column(name = "painted_by")
    private String paintedBy; // Username of the player who painted this pixel

    @Column(name = "painted_at")
    private Long paintedAt; // Timestamp

    @Version
    private Long version; // For optimistic locking
}
