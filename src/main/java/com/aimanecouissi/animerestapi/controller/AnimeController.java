package com.aimanecouissi.animerestapi.controller;

import com.aimanecouissi.animerestapi.enums.AnimeStatus;
import com.aimanecouissi.animerestapi.enums.AnimeType;
import com.aimanecouissi.animerestapi.payload.dto.AnimeDTO;
import com.aimanecouissi.animerestapi.payload.response.AnimePaginatedResponse;
import com.aimanecouissi.animerestapi.service.AnimeService;
import com.aimanecouissi.animerestapi.utility.ApplicationConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/anime")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Anime API", description = "Endpoints to manage anime resources.")
public class AnimeController {
    private final AnimeService animeService;

    public AnimeController(AnimeService animeService) {
        this.animeService = animeService;
    }

    @PostMapping
    @Operation(summary = "Create a new anime", description = "Endpoint to create a new anime entry.")
    @ApiResponse(responseCode = "201", description = "Anime successfully created.")
    public ResponseEntity<AnimeDTO> createAnime(@Valid @RequestBody AnimeDTO animeDTO) {
        AnimeDTO createdAnime = animeService.createAnime(animeDTO);
        return new ResponseEntity<>(createdAnime, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all anime", description = "Endpoint to retrieve a list of all anime entries.")
    @ApiResponse(responseCode = "200", description = "List of anime retrieved successfully.")
    public AnimePaginatedResponse getAllAnime(
            @RequestParam(value = "pageNo", defaultValue = ApplicationConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = ApplicationConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = ApplicationConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = ApplicationConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir
    ) {
        return animeService.getAllAnime(pageNo, pageSize, sortBy, sortDir);
    }

    @GetMapping("studio/{studio-id}")
    @Operation(summary = "Get anime by studio ID", description = "Endpoint to retrieve a list of anime by a specific studio ID.")
    @ApiResponse(responseCode = "200", description = "List of anime retrieved successfully.")
    public ResponseEntity<List<AnimeDTO>> getAnimeByStudioId(@PathVariable("studio-id") long studioId) {
        List<AnimeDTO> animeList = animeService.getAnimeByStudioId(studioId);
        return ResponseEntity.ok(animeList);
    }

    @GetMapping("{id}")
    @Operation(summary = "Get anime by ID", description = "Endpoint to retrieve details of an anime by its unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Anime details retrieved successfully."),
            @ApiResponse(responseCode = "404", description = "Anime not found.")
    })
    public ResponseEntity<AnimeDTO> getAnimeById(@PathVariable("id") long id) {
        AnimeDTO anime = animeService.getAnimeById(id);
        return new ResponseEntity<>(anime, HttpStatus.OK);
    }

    @PutMapping("{id}")
    @Operation(summary = "Update anime by ID", description = "Endpoint to update details of an existing anime by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Anime updated successfully."),
            @ApiResponse(responseCode = "404", description = "Anime not found.")
    })
    public ResponseEntity<AnimeDTO> updateAnime(@PathVariable("id") long id, @Valid @RequestBody AnimeDTO animeDTO) {
        AnimeDTO updatedAnime = animeService.updateAnime(id, animeDTO);
        return new ResponseEntity<>(updatedAnime, HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Delete anime by ID", description = "Endpoint to delete an anime by its unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Anime deleted successfully."),
            @ApiResponse(responseCode = "404", description = "Anime not found.")
    })
    public ResponseEntity<String> deleteAnime(@PathVariable("id") long id) {
        animeService.deleteAnime(id);
        return new ResponseEntity<>("The anime has been successfully deleted.", HttpStatus.OK);
    }

    @GetMapping("search")
    @Operation(summary = "Search anime by various attributes", description = "Endpoint to search for anime by various attributes.")
    @ApiResponse(responseCode = "200", description = "Anime list retrieved successfully.")
    public ResponseEntity<List<AnimeDTO>> searchAnime(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "type", required = false) AnimeType type,
            @RequestParam(value = "status", required = false) AnimeStatus status,
            @RequestParam(value = "rating", required = false) Integer rating,
            @RequestParam(value = "isFavorite", required = false) Boolean isFavorite,
            @RequestParam(value = "isComplete", required = false) Boolean isComplete
    ) {
        return ResponseEntity.ok(animeService.searchAnime(title, type, status, rating, isFavorite, isComplete));
    }

    @GetMapping("/mean-rating")
    @Operation(summary = "Get mean rating of anime", description = "Endpoint to retrieve the mean rating of all anime entries for the current user.")
    @ApiResponse(responseCode = "200", description = "Mean rating retrieved successfully.")
    public ResponseEntity<Double> getMeanRating() {
        Double meanRating = animeService.getMeanRating();
        return ResponseEntity.ok(meanRating);
    }
}
