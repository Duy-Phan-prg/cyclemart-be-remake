package com.example.cyclemartberemake.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "escrow")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Escrow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int orderId;

    private double amount;

    private String status;

    private String releaseType;

    private LocalDateTime holdAt;

    private LocalDateTime releaseAt;

    private LocalDateTime autoReleaseAt;
}