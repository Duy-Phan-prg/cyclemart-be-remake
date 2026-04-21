package com.example.cyclemartberemake.entity;

public enum InspectionStatus {
    PENDING,    // Chờ Admin phân công
    ASSIGNED,   // Đã phân công cho Inspector
    INSPECTING, // Đang kiểm tra tại chỗ
    PASSED,     // Đạt kiểm định
    FAILED,     // Không đạt
    CANCELED    // Hủy bỏ
}