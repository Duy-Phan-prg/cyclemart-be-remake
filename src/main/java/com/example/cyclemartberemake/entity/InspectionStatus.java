package com.example.cyclemartberemake.entity;

public enum InspectionStatus {
    PENDING_PAYMENT, // Chờ thanh toán phí kiểm định
    PENDING,         // Đã thanh toán, chờ Admin phân công
    ASSIGNED,        // Đã phân công cho Inspector
    INSPECTING,      // Đang kiểm tra tại chỗ
    PASSED,          // Đạt kiểm định
    FAILED,          // Không đạt
    CANCELED         // Hủy bỏ
}