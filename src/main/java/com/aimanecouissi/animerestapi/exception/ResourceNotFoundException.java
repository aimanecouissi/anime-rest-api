package com.aimanecouissi.animerestapi.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@Setter
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    private final String resource;
    private final String field;
    private final String value;

    public ResourceNotFoundException(String resource, String field, String value) {
        super(String.format("Resource '%s' not found with %s: '%s'.", resource, field, value));
        this.resource = resource;
        this.field = field;
        this.value = value;
    }
}
