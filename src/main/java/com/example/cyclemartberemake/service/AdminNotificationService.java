package com.example.cyclemartberemake.service;

import com.example.cyclemartberemake.dto.response.AdminNotificationResponse;

import java.util.List;

public interface AdminNotificationService {
    List<AdminNotificationResponse> getAdminNotifications();
    int getUnreadCount();
    void markAsRead(Long id);
    void markAllAsRead();
    void createNotification(String type, String title, String message, Integer count, String actionUrl);
}