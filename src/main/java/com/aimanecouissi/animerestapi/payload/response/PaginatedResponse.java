package com.aimanecouissi.animerestapi.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Schema(description = "Generic response object containing a list of items along with pagination details")
public class PaginatedResponse<T> {
    @Schema(description = "List of items")
    private List<T> items;

    @Schema(description = "Page number of the current result set", example = "0")
    private int pageNumber;

    @Schema(description = "Size of each page", example = "10")
    private int pageSize;

    @Schema(description = "Total number of elements across all pages", example = "100")
    private long totalElements;

    @Schema(description = "Total number of pages based on the pageSize", example = "10")
    private int totalPages;

    @Schema(description = "Flag indicating if this is the last page of results", example = "false")
    private boolean isLast;
}
