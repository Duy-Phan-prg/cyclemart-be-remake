package com.example.cyclemartberemake.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
@Builder
@Data
public class InspectionResponseDTO {
    private Long id;
    private Long postId;
    private String postTitle;
    private String sellerName;
    private String sellerPhone;

    private Long inspectorId;
    private String inspectorName;

    private LocalDateTime scheduledDateTime;
    private String status;
    private String resultNote;

    // --- THÔNG TIN NGƯỜI BÁN ĐIỀN KHI ĐĂNG KÝ ---

    private String address;
    private String sellerNote; // Chú ý: dùng tên này để Frontend nhận đúng

    private Double inspectionFee;
    private String checklistData;
    private LocalDateTime createdAt;
}