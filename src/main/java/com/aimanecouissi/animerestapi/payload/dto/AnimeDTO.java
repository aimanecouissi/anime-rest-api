package com.aimanecouissi.animerestapi.payload.dto;

import com.aimanecouissi.animerestapi.enums.AnimeStatus;
import com.aimanecouissi.animerestapi.enums.AnimeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "DTO representing Anime details")
public class AnimeDTO {
    @Schema(description = "Unique identifier of the Anime", example = "1")
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be less than or equal to 100 characters")
    @Schema(description = "Title of the Anime", example = "Attack on Titan")
    private String title;

    @Schema(description = "Type of the Anime (e.g., TV or Movie)")
    private AnimeType type;

    @NotNull(message = "Studio ID is required")
    @Schema(description = "ID of the Studio associated with the Anime", example = "1")
    private Long studioId;

    @Schema(description = "Status of the Anime (e.g., Watching, Completed or Plan to Watch)")
    private AnimeStatus status;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 10, message = "Rating must be at most 10")
    @Schema(description = "Rating of the Anime (1-10)")
    private Integer rating;

    @Schema(description = "Indicates whether the Anime is marked as favorite")
    private boolean isFavorite;

    @Schema(description = "Indicates whether the Anime is marked as complete")
    private boolean isComplete;
}
