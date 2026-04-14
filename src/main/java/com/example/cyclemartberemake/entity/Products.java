package com.example.cyclemartberemake.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Products {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int userId;

    private int shopId;

    private int categoryId;

    private String name;

    private double price;

    private String condition;

    private String status;

    private Integer reservedForUserId;

    private String hideReason;

    private LocalDateTime hiddenAt;

    private LocalDateTime createdAt;
}