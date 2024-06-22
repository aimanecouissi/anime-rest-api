package com.aimanecouissi.animerestapi.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum MangaStatus {
    READING("READING"),
    COMPLETED("COMPLETED"),
    PLAN_TO_READ("PLAN TO READ");

    private final String status;

    MangaStatus(String status) {
        this.status = status;
    }

    @JsonCreator
    public static MangaStatus fromValue(String value) {
        for (MangaStatus status : MangaStatus.values()) {
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
