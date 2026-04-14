package com.example.cyclemartberemake.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int conversationId;

    private int buyerId;

    private int sellerId;

    private int shopId;

    private int productId;

    private double dealAmount;

    private double depositAmount;

    private String status;

    private LocalDateTime shippedAt;

    private LocalDateTime buyerConfirmedAt;

    private LocalDateTime autoReleaseAt;

    private LocalDateTime orderedAt;
}