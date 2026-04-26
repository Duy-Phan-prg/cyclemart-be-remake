package com.example.cyclemartberemake.controller;

import com.example.cyclemartberemake.dto.response.AdminNotificationResponse;
import com.example.cyclemartberemake.service.AdminNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
@Tag(name = "Admin Notifications", description = "API quản lý thông báo cho Admin")
public class AdminNotificationController {

    private final AdminNotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách thông báo cho Admin")
    public ResponseEntity<List<AdminNotificationResponse>> getAdminNotifications() {
        List<AdminNotificationResponse> notifications = notificationService.getAdminNotifications();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy số lượng thông báo chưa đọc")
    public ResponseEntity<Integer> getUnreadCount() {
        int count = notificationService.getUnreadCount();
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{id}/mark-read")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Đánh dấu thông báo đã đọc")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/mark-all-read")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Đánh dấu tất cả thông báo đã đọc")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok().build();
    }
}