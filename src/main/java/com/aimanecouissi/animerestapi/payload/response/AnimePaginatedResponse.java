package com.aimanecouissi.animerestapi.payload.response;

import com.aimanecouissi.animerestapi.payload.dto.AnimeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Schema(description = "Response object containing a list of anime along with pagination details")
public class AnimePaginatedResponse extends PaginatedResponse<AnimeDTO> {
}
