package com.example.cyclemartberemake.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BikePostResponse {

    private Long id;
    private String title;
    private Double price;

    private String categoryName;

    private List<String> images;
}