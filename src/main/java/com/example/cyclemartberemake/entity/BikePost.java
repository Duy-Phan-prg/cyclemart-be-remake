package com.example.cyclemartberemake.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @Enumerated(EnumType.STRING)
    private City city;

    @Enumerated(EnumType.STRING)
    private HCMDistrict district;

    @Enumerated(EnumType.STRING)
    private BikeBrand brand;

    private String model;
    private Integer year;

    @Enumerated(EnumType.STRING)
    private FrameMaterial frameMaterial;

    @Enumerated(EnumType.STRING)
    private FrameSize frameSize;

    @Enumerated(EnumType.STRING)
    private BrakeType brakeType;

    @Enumerated(EnumType.STRING)
    private Groupset groupset;

    private Integer mileage;

    // 🔥 FIX CHUẨN FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(nullable = false)
    private Boolean allowNegotiation = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus postStatus;

    private Long approvedBy;
    private LocalDateTime approvedAt;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Categories category;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<BikeImage> images;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<PostPrioritySubscription> prioritySubscriptions;

    @Column(nullable = false)
    private Boolean isVerified = false;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (postStatus == null) postStatus = PostStatus.PENDING;
        if (allowNegotiation == null) allowNegotiation = false;
        if (isVerified == null) isVerified = false;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}