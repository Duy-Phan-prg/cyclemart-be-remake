package com.example.cyclemartberemake.entity;

public enum ReportType {
    FRAUD("Lừa đảo"),
    SPAM("Spam"),
    INAPPROPRIATE_CONTENT("Nội dung không phù hợp"),
    FAKE_PRODUCT("Sản phẩm giả"),
    PRICE_DISPUTE("Tranh chấp giá cả"),
    DELIVERY_ISSUE("Vấn đề giao hàng"),
    OTHER("Khác");

    private final String displayName;

    ReportType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}