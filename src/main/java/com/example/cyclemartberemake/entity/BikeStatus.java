package com.example.cyclemartberemake.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum BikeStatus {
    NEW("Mới 100%"),
    LIKE_NEW("Như mới (99%)"),
    GOOD("Đã dùng ít (90%+)"),
    USED("Đã dùng nhiều"),
    NEED_REPAIR("Cần sửa chữa");

    private final String displayName;

    BikeStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static BikeStatus from(String value) {
        return BikeStatus.valueOf(value.toUpperCase());
    }
}