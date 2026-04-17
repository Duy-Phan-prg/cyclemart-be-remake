package com.example.cyclemartberemake.entity;

public enum FrameSize {
    XS("XS"),
    S("S"),
    M("M"),
    L("L"),
    XL("XL"),
    SIZE_47("47cm"),
    SIZE_49("49cm"),
    SIZE_51("51cm"),
    SIZE_53("53cm"),
    SIZE_55("55cm"),
    SIZE_57("57cm"),
    SIZE_59("59cm"),
    SIZE_61("61cm");

    private final String displayName;

    FrameSize(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}