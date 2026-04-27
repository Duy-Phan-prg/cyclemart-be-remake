package com.example.cyclemartberemake.entity;

public enum PaymentType {
    PRIORITY_PACKAGE,    // Thanh toán gói ưu tiên
    INSPECTION_FEE,      // Thanh toán phí kiểm định
    ORDER_DEPOSIT,       // Đặt cọc mua xe
    POINT_RECHARGE,      // Nạp điểm vào tài khoản
    ORDER_PAYMENT,       // Mua xe có kiểm định (online/escrow)
    DIRECT_PAYMENT,      // Mua xe chưa kiểm định (COD - thanh toán trực tiếp)
    OTHER
}
