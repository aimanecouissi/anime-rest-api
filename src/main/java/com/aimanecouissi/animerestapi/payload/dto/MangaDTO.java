package com.aimanecouissi.animerestapi.payload.dto;

import com.aimanecouissi.animerestapi.enums.MangaStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "DTO representing manga details")
public class MangaDTO {
    @Schema(description = "Unique identifier for the manga")
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be less than or equal to 100 characters")
    @Schema(description = "Title of the manga", example = "One Piece")
    private String title;

    @NotNull(message = "Status is required")
    @Schema(description = "Status of the manga", example = "READING")
    private MangaStatus status;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 10, message = "Rating must be at most 10")
    @Schema(description = "Rating of the manga", example = "9")
    private Integer rating;

    @Schema(description = "Indicates if the manga is marked as favorite")
    private boolean isFavorite;
}
