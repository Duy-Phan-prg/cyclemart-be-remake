package com.example.cyclemartberemake.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentLogs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int paymentId;

    private String status;

    @Column(columnDefinition = "json")
    private String rawResponse;

    private LocalDateTime createdAt;
}