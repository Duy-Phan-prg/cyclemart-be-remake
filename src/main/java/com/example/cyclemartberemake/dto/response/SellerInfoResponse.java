package com.example.cyclemartberemake.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerInfoResponse {

    private Long sellerId;
    private String sellerName;
    private String sellerEmail;
    private Double averageScore;
    private Long totalRatings;
}
