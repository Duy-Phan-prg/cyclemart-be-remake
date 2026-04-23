package com.example.cyclemartberemake.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class InspectionResponseDTO {
    private Long id;
    private Long postId;
    private String postTitle;
    private String sellerName;
    private String inspectorName;
    private String sellerPhone;
    private String status;
    private String address;
    private LocalDateTime scheduledDateTime;
    private Double inspectionFee;
    private String note;
    private String resultNote;
    private LocalDateTime createdAt;
    private String checklistData;
}