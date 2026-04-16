package com.example.cyclemartberemake.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bike_posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BikePost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Double price;

    @Enumerated(EnumType.STRING)
    private BikeStatus status;


    private String city;
    private String district;

    private String brand;
    private String model;
    private Integer year;

    private String frameMaterial;
    private String frameSize;
    private String brakeType;
    private String groupset;
    private Integer mileage;

    private Long userId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Categories category;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<BikeImage> images;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
