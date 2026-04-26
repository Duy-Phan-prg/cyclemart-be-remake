package com.example.cyclemartberemake.entity;

public enum OrderStatus {
    PENDING_PAYMENT,          // Chờ thanh toán
    PAID_WAITING_DELIVERY,    // Đã thanh toán - Chờ giao hàng
    IN_DELIVERY,              // Đang vận chuyển
    DELIVERED,                // Đã nhận hàng
    COMPLETED,                // Hoàn tất (Đã đánh giá)
    RETURN_REQUESTED,         // Yêu cầu hoàn trả
    AWAITING_DISPUTE_DEPOSIT, // Chờ nộp cọc tranh chấp
    DISPUTE_SYSTEM,           // Đang tranh chấp
    CANCELLED                 // Đã hủy
}