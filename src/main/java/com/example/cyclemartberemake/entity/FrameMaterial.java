package com.example.cyclemartberemake.entity;

public enum FrameMaterial {
    CARBON("Carbon"),
    ALUMINUM("Aluminum"),
    STEEL("Steel"),
    TITANIUM("Titanium"),
    ALLOY("Alloy"),
    CHROMOLY("Chromoly");

    private final String displayName;

    FrameMaterial(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}