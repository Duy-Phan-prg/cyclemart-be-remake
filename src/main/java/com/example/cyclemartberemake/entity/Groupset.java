package com.example.cyclemartberemake.entity;

public enum Groupset {
    // Shimano Road
    SHIMANO_DURA_ACE("Shimano Dura-Ace"),
    SHIMANO_ULTEGRA("Shimano Ultegra"),
    SHIMANO_105("Shimano 105"),
    SHIMANO_TIAGRA("Shimano Tiagra"),
    SHIMANO_SORA("Shimano Sora"),
    SHIMANO_CLARIS("Shimano Claris"),
    
    // Shimano MTB
    SHIMANO_XTR("Shimano XTR"),
    SHIMANO_XT("Shimano XT"),
    SHIMANO_SLX("Shimano SLX"),
    SHIMANO_DEORE("Shimano Deore"),
    
    // SRAM
    SRAM_RED("SRAM Red"),
    SRAM_FORCE("SRAM Force"),
    SRAM_RIVAL("SRAM Rival"),
    SRAM_APEX("SRAM Apex"),
    
    // Campagnolo
    CAMPAGNOLO_SUPER_RECORD("Campagnolo Super Record"),
    CAMPAGNOLO_RECORD("Campagnolo Record"),
    CAMPAGNOLO_CHORUS("Campagnolo Chorus"),
    
    OTHER("Khác");

    private final String displayName;

    Groupset(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}