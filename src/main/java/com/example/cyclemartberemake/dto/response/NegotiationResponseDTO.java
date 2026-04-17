package com.example.cyclemartberemake.dto.response;

import com.example.cyclemartberemake.entity.NegotiationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NegotiationResponseDTO {
    private Long id;
    private Long buyerId;
    private Long bikePostId;
    private Double offeredPrice;
    private Double counterPrice;
    private NegotiationStatus status;
    private LocalDateTime createdAt;
}