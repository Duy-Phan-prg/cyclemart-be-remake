package com.example.cyclemartberemake.entity;

public enum BikeBrand {
    GIANT("Giant"),
    TREK("Trek"),
    SPECIALIZED("Specialized"),
    CANNONDALE("Cannondale"),
    SCOTT("Scott"),
    MERIDA("Merida"),
    BIANCHI("Bianchi"),
    PINARELLO("Pinarello"),
    CERVELO("Cervelo"),
    COLNAGO("Colnago"),
    BMC("BMC"),
    ORBEA("Orbea"),
    CUBE("Cube"),
    FOCUS("Focus"),
    CANYON("Canyon"),
    SANTA_CRUZ("Santa Cruz"),
    THONG_NHAT("Thống Nhất"),
    ASAMA("Asama"),
    FORNIX("Fornix"),
    OTHER("Khác");

    private final String displayName;

    BikeBrand(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}