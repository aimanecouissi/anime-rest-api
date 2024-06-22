package com.aimanecouissi.animerestapi.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum AnimeType {
    TV("TV"),
    MOVIE("MOVIE");

    private final String type;

    AnimeType(String type) {
        this.type = type;
    }

    @JsonCreator
    public static AnimeType fromValue(String value) {
        for (AnimeType type : AnimeType.values()) {
            if (type.getType().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown type: " + value);
    }

    @JsonValue
    public String getType() {
        return type;
    }
}