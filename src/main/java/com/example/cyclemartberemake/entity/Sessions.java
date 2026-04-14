package com.example.cyclemartberemake.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sessions {

    @Id
    private String id;

    private int userId;

    private String guestSessionId;

    private String token;

    private LocalDateTime expiresAt;

    private LocalDateTime revokedAt;

    private LocalDateTime convertedAt;

    private LocalDateTime createdAt;
}