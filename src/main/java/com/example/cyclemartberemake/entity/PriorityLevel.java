package com.example.cyclemartberemake.entity;

public enum PriorityLevel {
    SILVER(3),           // Mức thấp - hiển thị bình thường
    GOLD(2),             // Mức trung bình - nổi bật hơn
    PLATINUM(1);         // Mức cao - ưu tiên đầu tiên trong category

    private final Integer levelValue;

    PriorityLevel(Integer levelValue) {
        this.levelValue = levelValue;
    }

    public Integer getLevelValue() {
        return levelValue;
    }
}


