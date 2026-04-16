package com.example.cyclemartberemake.entity;

public enum ReportStatus {
    PENDING("Chờ xử lý"),
    IN_PROGRESS("Đang xử lý"), 
    RESOLVED("Đã giải quyết"),
    REJECTED("Từ chối");

    private final String displayName;

    ReportStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}