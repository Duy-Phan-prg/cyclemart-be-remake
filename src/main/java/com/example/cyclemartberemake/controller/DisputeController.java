package com.example.cyclemartberemake.controller;

import com.example.cyclemartberemake.dto.request.DisputeRequest;
import com.example.cyclemartberemake.dto.response.DisputeResponse;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.service.DisputeService;
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
@RequestMapping("/api/v1/disputes")
@RequiredArgsConstructor
@Tag(name = "Dispute Management", description = "APIs for buyer-seller dispute handling")
public class DisputeController {

    private final DisputeService disputeService;

    @PostMapping
    @Operation(summary = "Buyer mở tranh chấp sau khi nhận hàng (trong 7 ngày)")
    public ResponseEntity<DisputeResponse> openDispute(@Valid @RequestBody DisputeRequest request) {
        return ResponseEntity.ok(disputeService.openDispute(getCurrentUserId(), request));
    }

    @PutMapping("/{id}/seller-approve")
    @Operation(summary = "Seller chấp nhận hoàn hàng")
    public ResponseEntity<DisputeResponse> sellerApprove(@PathVariable Long id) {
        return ResponseEntity.ok(disputeService.sellerApprove(id, getCurrentUserId()));
    }

    @PutMapping("/{id}/seller-reject")
    @Operation(summary = "Seller từ chối - chuyển sang Admin xem xét")
    public ResponseEntity<DisputeResponse> sellerReject(@PathVariable Long id) {
        return ResponseEntity.ok(disputeService.sellerReject(id, getCurrentUserId()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/admin-resolve")
    @Operation(summary = "Admin ra quyết định: REFUND_BUYER | RELEASE_SELLER | PARTIAL")
    public ResponseEntity<DisputeResponse> adminResolve(
            @PathVariable Long id,
            @RequestParam String resolution,
            @RequestParam(required = false) String resolutionNote) {
        return ResponseEntity.ok(disputeService.adminResolve(id, getCurrentUserId(), resolution, resolutionNote));
    }

    @GetMapping("/my/buyer")
    @Operation(summary = "Buyer xem tranh chấp của mình")
    public Page<DisputeResponse> getMyBuyerDisputes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return disputeService.getMyDisputesAsBuyer(getCurrentUserId(),
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    @GetMapping("/my/seller")
    @Operation(summary = "Seller xem tranh chấp liên quan đến mình")
    public Page<DisputeResponse> getMySellerDisputes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return disputeService.getMyDisputesAsSeller(getCurrentUserId(),
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    @Operation(summary = "Admin xem tất cả tranh chấp")
    public Page<DisputeResponse> getAllForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return disputeService.getAllForAdmin(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Users user) {
            return (long) user.getId();
        }
        throw new RuntimeException("Người dùng chưa đăng nhập");
    }
}
