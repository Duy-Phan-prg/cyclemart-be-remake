package com.example.cyclemartberemake.entity;

public enum BrakeType {
    DISC_HYDRAULIC("Disc Hydraulic"),
    DISC_MECHANICAL("Disc Mechanical"),
    RIM_BRAKE("Rim Brake"),
    V_BRAKE("V-Brake"),
    CANTILEVER("Cantilever");

    private final String displayName;

    BrakeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}