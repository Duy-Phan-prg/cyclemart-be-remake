package com.example.cyclemartberemake.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class WishlistItemResponse {
    private Long wishlistItemId;
    private LocalDateTime addedAt;

    private Long postId;
    private String title;
    private Double price;
    private String postStatus;
    private String city;
    private String brand;

    private List<String> images;

    private Long sellerId;
    private String sellerName;
}
