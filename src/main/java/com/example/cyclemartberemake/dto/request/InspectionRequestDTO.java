package com.example.cyclemartberemake.dto.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InspectionRequestDTO {
    private Long postId;
    private String address;
    private LocalDateTime scheduledDateTime;
    private String note;
}