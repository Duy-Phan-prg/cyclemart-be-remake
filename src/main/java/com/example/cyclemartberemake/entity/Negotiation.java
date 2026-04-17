package com.example.cyclemartberemake.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "negotiations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Negotiation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long buyerId;

    @ManyToOne
    @JoinColumn(name = "bike_post_id")
    private BikePost bikePost;

    private Double offeredPrice;
    private Double counterPrice;

    @Enumerated(EnumType.STRING)
    private NegotiationStatus status;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}