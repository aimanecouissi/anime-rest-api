package com.aimanecouissi.animerestapi.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum AnimeStatus {
    WATCHING("WATCHING"),
    COMPLETED("COMPLETED"),
    PLAN_TO_WATCH("PLAN TO WATCH");

    private final String status;

    AnimeStatus(String status) {
        this.status = status;
    }

    @JsonCreator
    public static AnimeStatus fromValue(String value) {
        for (AnimeStatus status : AnimeStatus.values()) {
            if (status.getStatus().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }

    @JsonValue
    public String getStatus() {
        return status;
    }
}