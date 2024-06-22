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
public class SingleErrorResponse extends ErrorResponse {
    @Schema(description = "Error message providing more details about the issue", example = "The resource you requested could not be found.")
    private String message;

    public SingleErrorResponse(LocalDateTime timestamp, int status, String error, String message) {
        super(timestamp, status, error);
        this.message = message;
    }
}
