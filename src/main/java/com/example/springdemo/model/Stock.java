package com.example.springdemo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data // 自动生成 Getter, Setter, toString
@AllArgsConstructor
@NoArgsConstructor
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer quantity;

    // ✨ 魔法字段：JPA 会自动维护它
    // 每次更新，version 会自动 +1
    @Version
    private Integer version;
}
