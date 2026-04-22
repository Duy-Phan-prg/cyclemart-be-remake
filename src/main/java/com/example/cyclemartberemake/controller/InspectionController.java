package com.example.cyclemartberemake.controller;

import com.example.cyclemartberemake.dto.request.InspectionRequestDTO;
import com.example.cyclemartberemake.dto.response.InspectionResponseDTO;
import com.example.cyclemartberemake.entity.Inspection;
import com.example.cyclemartberemake.repository.InspectionRepository;
import com.example.cyclemartberemake.service.InspectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.LocalDateTime;


@RestController
@RequestMapping("/api/v1/inspections")
@RequiredArgsConstructor
public class InspectionController {

    private final InspectionService inspectionService;
    private final InspectionRepository inspectionRepository;

    // 1. User tạo yêu cầu (Ai đăng nhập cũng được)
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public InspectionResponseDTO createRequest(@Valid @RequestBody InspectionRequestDTO request) {
        return inspectionService.createRequest(request);
    }

    // 2. User xem lịch sử yêu cầu của mình
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public Page<InspectionResponseDTO> getMyRequests(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return inspectionService.getMyRequests(PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    // 3. Admin xem tất cả
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    @GetMapping("/admin/all")
    public Page<InspectionResponseDTO> getAllRequests(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return inspectionService.getAllRequests(PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    // 4. Admin phân công
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    @PutMapping("/admin/{id}/assign")
    public void assignInspector(@PathVariable Long id, @RequestParam Long inspectorId) {
        inspectionService.assignInspector(id, inspectorId);
    }

    // 5. Inspector xem việc của mình
    @PreAuthorize("hasAnyAuthority('INSPECTOR', 'ROLE_INSPECTOR')")
    @GetMapping("/inspector/me")
    public Page<InspectionResponseDTO> getInspectorTasks(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return inspectionService.getRequestsForInspector(PageRequest.of(page, size, Sort.by("scheduledDateTime").ascending()));
    }

    // 6. Inspector cập nhật kết quả
    @PreAuthorize("hasAnyAuthority('INSPECTOR', 'ROLE_INSPECTOR')")
    @PutMapping("/{id}/result") // 🔥 ĐỔI PATH Ở ĐÂY CHO KHỚP VỚI FRONTEND
    public void updateResult(@PathVariable Long id, @RequestParam String status, @RequestParam String resultNote) {
        // Chú ý: Backend đang nhận param tên là 'resultNote' (có thể là 'note' trong service, hãy đảm bảo service của bạn nhận đúng)
        inspectionService.updateResult(id, status, resultNote);
    }

    // 7. Admin đổi lại lịch
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    @PutMapping("/admin/{id}/reschedule")
    public void reschedule(@PathVariable Long id, @RequestParam LocalDateTime newTime) {
        Inspection ins = inspectionRepository.findById(id).orElseThrow();
        ins.setScheduledDateTime(newTime);
        inspectionRepository.save(ins);
    }
}