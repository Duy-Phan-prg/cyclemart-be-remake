package com.example.cyclemartberemake.entity;

public enum City {
    HO_CHI_MINH("TP. Hồ Chí Minh"),
    HA_NOI("Hà Nội"),
    DA_NANG("Đà Nẵng");

    private final String displayName;

    City(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}