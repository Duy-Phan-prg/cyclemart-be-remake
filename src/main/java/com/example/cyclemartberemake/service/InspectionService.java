package com.example.cyclemartberemake.service;

import com.example.cyclemartberemake.dto.request.InspectionRequestDTO;
import com.example.cyclemartberemake.dto.response.InspectionResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InspectionService {

    // Các luồng nghiệp vụ kiểm định cơ bản
    InspectionResponseDTO createRequest(InspectionRequestDTO request);
    Page<InspectionResponseDTO> getMyRequests(Pageable pageable);
    Page<InspectionResponseDTO> getAllRequests(Pageable pageable); // Cho Admin
    Page<InspectionResponseDTO> getRequestsForInspector(Pageable pageable); // Cho Inspector

    void assignInspector(Long inspectionId, Long inspectorId);
    void updateResult(Long inspectionId, String status, String resultNote, String checklistData);

    // Cập nhật mới: Quản lý mức phí kiểm định chung cho toàn bộ hệ thống
    Double getGlobalInspectionFee();
    void updateGlobalInspectionFee(Double fee);
}