package com.example.cyclemartberemake.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reviews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int orderId;

    private int reviewerId;

    private int sellerId;

    private int shopId;

    private double rating;

    @Column(columnDefinition = "json")
    private String tags;

    private String content;

    private String reviewerRole;

    private String status;

    private LocalDateTime createdAt;
}