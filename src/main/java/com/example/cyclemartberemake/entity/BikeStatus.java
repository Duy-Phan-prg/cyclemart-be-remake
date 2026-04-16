package com.example.cyclemartberemake.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum BikeStatus {
    NEW,
    LIKE_NEW,
    USED,
    NEED_REPAIR;

    @JsonCreator
    public static BikeStatus from(String value) {
        return BikeStatus.valueOf(value.toUpperCase());
    }
}