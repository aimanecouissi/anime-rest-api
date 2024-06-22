package com.aimanecouissi.animerestapi.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Schema(description = "Response object for single error details")
public class ErrorResponse {
    @Schema(description = "Timestamp when the error occurred", example = "2024-06-13T10:15:30Z")
    protected LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "404")
    protected int status;

    @Schema(description = "Error type", example = "Not Found")
    protected String error;
}
