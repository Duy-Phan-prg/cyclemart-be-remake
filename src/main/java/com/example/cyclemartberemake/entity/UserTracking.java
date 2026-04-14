package com.example.cyclemartberemake.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_tracking")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int userId;

    private String sessionId;

    private String eventType;

    private String location;

    private Integer productId;

    private Integer sellerId;

    private Integer categoryId;

    private LocalDateTime createdAt;
}