package com.example.cyclemartberemake.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "listing_boosts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListingBoosts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int productId;

    private int userId;

    private int packageId;

    private String paymentStatus;

    private String paymentRef;

    private LocalDateTime startedAt;

    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;
}