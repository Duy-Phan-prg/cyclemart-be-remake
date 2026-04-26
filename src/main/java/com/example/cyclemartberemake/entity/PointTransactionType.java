package com.example.cyclemartberemake.entity;

public enum PointTransactionType {
    TOP_UP_VNPAY,       // VNPay thanh cong -> quy doi thanh diem
    ESCROW_HOLD,        // Giu diem cho giao dich mua hang
    ESCROW_RELEASE,     // Giai phong diem cho Seller sau giao nhan
    ESCROW_REFUND,      // Hoan diem ve Buyer khi tranh chap
    INSPECTION_FEE,     // Tru diem phi kiem dinh
    PRIORITY_PACKAGE,   // Tru diem mua goi uu tien
    WITHDRAW_REQUEST,   // Seller tao yeu cau rut tien
    WITHDRAW_APPROVED,  // Admin duyet rut tien
    WITHDRAW_REJECTED   // Admin tu choi rut tien, hoan lai diem
}
