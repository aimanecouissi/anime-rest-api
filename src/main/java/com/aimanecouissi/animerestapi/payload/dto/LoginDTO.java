package com.aimanecouissi.animerestapi.payload.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "DTO representing user credentials for login")
public class LoginDTO {
    @NotBlank(message = "Username is required")
    @Schema(description = "Username of the user", example = "john_doe")
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "Password of the user", example = "P@ssw0rd")
    private String password;
}
