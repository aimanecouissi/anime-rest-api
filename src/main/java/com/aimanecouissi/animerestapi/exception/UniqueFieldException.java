package com.aimanecouissi.animerestapi.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@Setter
@ResponseStatus(value = HttpStatus.CONFLICT)
public class UniqueFieldException extends RuntimeException {
    private final String field;
    private final String value;

    public UniqueFieldException(String field, String value) {
        super(String.format("'%s' with value '%s' already exists.", field, value));
        this.field = field;
        this.value = value;
    }
}
