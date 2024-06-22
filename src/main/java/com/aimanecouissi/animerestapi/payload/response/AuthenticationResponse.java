package com.aimanecouissi.animerestapi.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Response object containing authentication token details")
public class AuthenticationResponse {
    @Schema(description = "Access token for authentication", example = "eyJhbGciOiJIUzI1NiIsIn...")
    private String accessToken;

    @Schema(description = "Type of token, typically 'Bearer'", example = "Bearer")
    private String tokenType;
}
