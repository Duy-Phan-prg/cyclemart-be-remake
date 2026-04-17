package com.example.cyclemartberemake.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BikePostResponse {

    private Long id;
    private String title;
    private String description;
    private Double price;

    private String status;

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

    private String categoryName;

    private Boolean allowNegotiation;

    private List<String> images;

    @Schema(description = "Priority package info (if post has active priority)")
    private PriorityPackageResponse activePriority;

    private LocalDateTime createdAt;
}