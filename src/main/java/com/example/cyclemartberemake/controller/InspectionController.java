package com.example.cyclemartberemake.controller;

import com.example.cyclemartberemake.dto.request.InspectionRequestDTO;
import com.example.cyclemartberemake.dto.response.InspectionResponseDTO;
import com.example.cyclemartberemake.entity.Inspection;
import com.example.cyclemartberemake.repository.InspectionRepository; // Import Reposiotry
import com.example.cyclemartberemake.service.InspectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/inspections")
@RequiredArgsConstructor
public class InspectionController {

    private final InspectionService inspectionService;
    private final InspectionRepository inspectionRepository; // 🔥 THÊM DÒNG NÀY ĐỂ HÀM RESCHEDULE HOẠT ĐỘNG

    // 1. User tạo yêu cầu
    @PostMapping
    public InspectionResponseDTO createRequest(@RequestBody InspectionRequestDTO request) {
        return inspectionService.createRequest(request);
    }

    // 2. User xem lịch sử yêu cầu của mình
    @GetMapping("/me")
    public Page<InspectionResponseDTO> getMyRequests(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return inspectionService.getMyRequests(PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    // 3. Admin xem tất cả
    @GetMapping("/admin/all")
    public Page<InspectionResponseDTO> getAllRequests(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return inspectionService.getAllRequests(PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    // 4. Admin phân công
    @PutMapping("/admin/{id}/assign")
    public void assignInspector(@PathVariable Long id, @RequestParam Integer inspectorId) {
        inspectionService.assignInspector(id, inspectorId);
    }

    // 5. Inspector xem việc của mình
    @GetMapping("/inspector/me")
    public Page<InspectionResponseDTO> getInspectorTasks(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        // 🔥 SỬA Ở ĐÂY: Đổi "scheduledDate" thành "scheduledDateTime"
        return inspectionService.getRequestsForInspector(PageRequest.of(page, size, Sort.by("scheduledDateTime").ascending()));
    }

    // 6. Inspector cập nhật kết quả
    @PutMapping("/inspector/{id}/result")
    public void updateResult(@PathVariable Long id, @RequestParam String status, @RequestParam String note) {
        inspectionService.updateResult(id, status, note);
    }

    // 7. Admin đổi lại lịch
    @PutMapping("/admin/{id}/reschedule")
    public void reschedule(@PathVariable Long id, @RequestParam LocalDateTime newTime) {
        Inspection ins = inspectionRepository.findById(id).orElseThrow();
        ins.setScheduledDateTime(newTime);
        inspectionRepository.save(ins);
    }
}