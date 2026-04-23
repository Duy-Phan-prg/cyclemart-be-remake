package com.example.cyclemartberemake.controller;

import com.example.cyclemartberemake.dto.request.CreatePaymentRequest;
import com.example.cyclemartberemake.dto.response.CreatePaymentResponse;
import com.example.cyclemartberemake.dto.response.PaymentResponse;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "APIs for payment processing and history")
public class PaymentController extends BaseController {

    private final PaymentService paymentService;

    @PostMapping("/sepay/create")
    @Operation(summary = "Create Sepay payment")
    public ResponseEntity<CreatePaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            HttpServletRequest httpRequest
    ) {
        try {
            CreatePaymentResponse response = paymentService.createPayment(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                CreatePaymentResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build()
            );
        }
    }

    @PostMapping("/sepay/ipn")
    @Operation(summary = "Sepay IPN callback (internal use)")
    public ResponseEntity<String> handleIPN(@RequestBody Map<String, Object> data) {
        System.out.println("=== IPN DATA RECEIVED: " + data);
        System.out.println("=== IPN DATA KEYS: " + data.keySet());
        try {
            paymentService.handleIPN(data);
            System.out.println("=== IPN PROCESSED SUCCESSFULLY");
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            System.out.println("=== IPN ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("ERROR: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    @Operation(summary = "Get payment history of current user")
    public Page<PaymentResponse> getPaymentHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? 
            Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        return paymentService.getPaymentHistory(pageable);
    }

    @GetMapping("/history/status/{status}")
    @Operation(summary = "Get payment history by status")
    public Page<PaymentResponse> getPaymentHistoryByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return paymentService.getPaymentHistoryByStatus(status, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment detail by ID")
    public PaymentResponse getPaymentById(@PathVariable Long id) {
        return paymentService.getPaymentById(id);
    }

    @PostMapping("/cleanup-expired")
    @Operation(summary = "Cleanup expired pending payments (admin only)")
    public ResponseEntity<String> cleanupExpiredPayments() {
        try {
            paymentService.cleanupExpiredPayments();
            return ResponseEntity.ok("Cleanup completed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Cleanup failed: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Refund a payment (admin only)")
    public ResponseEntity<?> refundPayment(
            @PathVariable Long id, 
            @RequestParam String reason
    ) {
        try {
            Long adminId = getCurrentUserId(); // For now, use current user as admin
            
            PaymentResponse response = paymentService.refundPayment(id, reason, adminId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a pending payment (user can cancel their own payment)")
    public ResponseEntity<?> cancelPayment(
            @PathVariable Long id, 
            @RequestParam(defaultValue = "Hủy bởi người dùng") String reason
    ) {
        try {
            PaymentResponse response = paymentService.cancelPayment(id, reason);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }


    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}