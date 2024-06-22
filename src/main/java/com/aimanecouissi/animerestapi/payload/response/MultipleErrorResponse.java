package com.aimanecouissi.animerestapi.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Schema(description = "Response object for multiple errors details")
public class MultipleErrorResponse extends ErrorResponse {
    @Schema(description = "Error message providing more details about the issue", example = "The resource you requested could not be found.")
    private Map<String, String> message;

    public MultipleErrorResponse(LocalDateTime timestamp, int status, String error, Map<String, String> message) {
        super(timestamp, status, error);
        this.message = message;
    }
}
