package com.example.cyclemartberemake.entity;

public enum City {
    HO_CHI_MINH("TP. Hồ Chí Minh");

    private final String displayName;

    City(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}