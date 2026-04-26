package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.response.AdminNotificationResponse;
import com.example.cyclemartberemake.entity.AdminNotification;
import com.example.cyclemartberemake.entity.PostStatus;
import com.example.cyclemartberemake.repository.AdminNotificationRepository;
import com.example.cyclemartberemake.repository.BikePostRepository;
import com.example.cyclemartberemake.service.AdminNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminNotificationServiceImpl implements AdminNotificationService {

    private final BikePostRepository bikePostRepository;
    private final AdminNotificationRepository adminNotificationRepository;

    @Override
    public List<AdminNotificationResponse> getAdminNotifications() {
        List<AdminNotificationResponse> notifications = new ArrayList<>();

        try {
            // Lấy notifications từ database nếu có
            List<AdminNotification> dbNotifications = adminNotificationRepository.findAllByOrderByCreatedAtDesc();
            
            // Tự động tạo notification cho tin đăng chờ duyệt
            long pendingPostsCount = bikePostRepository.countByPostStatus(PostStatus.PENDING);
            if (pendingPostsCount > 0) {
                // Kiểm tra xem đã có notification cho pending posts chưa
                List<AdminNotification> existingPendingNotifications = 
                    adminNotificationRepository.findByTypeAndIsReadFalse("POST_PENDING");
                
                if (existingPendingNotifications.isEmpty()) {
                    createNotification(
                        "POST_PENDING",
                        "Tin đăng mới cần duyệt",
                        String.format("Có %d tin đăng mới đang chờ duyệt", pendingPostsCount),
                        (int) pendingPostsCount,
                        "/admin/listings?status=PENDING"
                    );
                    // Refresh danh sách sau khi tạo mới
                    dbNotifications = adminNotificationRepository.findAllByOrderByCreatedAtDesc();
                } else {
                    // Cập nhật count cho notification hiện tại
                    AdminNotification existingNotification = existingPendingNotifications.get(0);
                    existingNotification.setCount((int) pendingPostsCount);
                    existingNotification.setMessage(String.format("Có %d tin đăng mới đang chờ duyệt", pendingPostsCount));
                    adminNotificationRepository.save(existingNotification);
                }
            }

            // Convert sang DTO
            notifications = dbNotifications.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            // Fallback nếu database chưa có bảng admin_notifications
            System.out.println("Database table not found, using fallback logic: " + e.getMessage());
            
            // Tạo notification tạm thời cho pending posts
            long pendingPostsCount = bikePostRepository.countByPostStatus(PostStatus.PENDING);
            if (pendingPostsCount > 0) {
                notifications.add(AdminNotificationResponse.builder()
                        .id(1L)
                        .type("POST_PENDING")
                        .title("Tin đăng mới cần duyệt")
                        .message(String.format("Có %d tin đăng mới đang chờ duyệt", pendingPostsCount))
                        .count((int) pendingPostsCount)
                        .actionUrl("/admin/listings?status=PENDING")
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build());
            }
        }

        return notifications;
    }

    private AdminNotificationResponse convertToResponse(AdminNotification notification) {
        return AdminNotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .count(notification.getCount())
                .actionUrl(notification.getActionUrl())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    @Override
    public int getUnreadCount() {
        try {
            return adminNotificationRepository.countByIsReadFalse();
        } catch (Exception e) {
            // Fallback nếu database chưa có bảng
            long pendingPostsCount = bikePostRepository.countByPostStatus(PostStatus.PENDING);
            return (int) pendingPostsCount;
        }
    }

    @Override
    @Transactional
    public void markAsRead(Long id) {
        try {
            AdminNotification notification = adminNotificationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Notification not found"));
            
            notification.setIsRead(true);
            adminNotificationRepository.save(notification);
        } catch (Exception e) {
            // Fallback - không làm gì nếu database chưa có bảng
            System.out.println("Cannot mark as read, database table not found: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        try {
            adminNotificationRepository.markAllAsRead();
        } catch (Exception e) {
            // Fallback - không làm gì nếu database chưa có bảng
            System.out.println("Cannot mark all as read, database table not found: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void createNotification(String type, String title, String message, Integer count, String actionUrl) {
        try {
            AdminNotification notification = AdminNotification.builder()
                    .type(type)
                    .title(title)
                    .message(message)
                    .count(count)
                    .actionUrl(actionUrl)
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            adminNotificationRepository.save(notification);
        } catch (Exception e) {
            // Fallback - không làm gì nếu database chưa có bảng
            System.out.println("Cannot create notification, database table not found: " + e.getMessage());
        }
    }
}