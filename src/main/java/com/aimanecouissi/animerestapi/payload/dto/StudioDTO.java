package com.aimanecouissi.animerestapi.payload.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "DTO representing studio details")
public class StudioDTO {
    @Schema(description = "Unique identifier for the studio", example = "1")
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 50, message = "Name must be less than or equal to 50 characters")
    @Schema(description = "Name of the studio", example = "Studio Ghibli")
    private String name;
}
