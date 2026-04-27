package com.example.cyclemartberemake.controller;

import com.example.cyclemartberemake.dto.request.WithdrawalRequestDTO;
import com.example.cyclemartberemake.dto.response.WithdrawalResponse;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.service.WithdrawalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/withdrawals")
@RequiredArgsConstructor
@Tag(name = "Withdrawal", description = "APIs for point withdrawal requests")
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "User tạo yêu cầu rút điểm")
    public ResponseEntity<WithdrawalResponse> createRequest(@Valid @RequestBody WithdrawalRequestDTO dto) {
        return ResponseEntity.ok(withdrawalService.createRequest(getCurrentUserId(), dto));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "User xem lịch sử yêu cầu rút của mình")
    public ResponseEntity<Page<WithdrawalResponse>> getMyRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(withdrawalService.getMyRequests(getCurrentUserId(),
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin xem tất cả yêu cầu rút")
    public ResponseEntity<Page<WithdrawalResponse>> getAllForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(withdrawalService.getAllForAdmin(
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @PutMapping("/admin/{id}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin xác nhận đã chuyển khoản")
    public ResponseEntity<WithdrawalResponse> complete(
            @PathVariable Long id,
            @RequestParam(required = false) String note) {
        return ResponseEntity.ok(withdrawalService.complete(id, getCurrentUserId(), note));
    }

    @PutMapping("/admin/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin từ chối yêu cầu rút (hoàn điểm về user)")
    public ResponseEntity<WithdrawalResponse> reject(
            @PathVariable Long id,
            @RequestParam(required = false) String note) {
        return ResponseEntity.ok(withdrawalService.reject(id, getCurrentUserId(), note));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Users user) {
            return (long) user.getId();
        }
        throw new RuntimeException("Người dùng chưa đăng nhập");
    }
}
