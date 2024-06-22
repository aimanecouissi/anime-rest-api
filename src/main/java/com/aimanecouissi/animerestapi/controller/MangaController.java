package com.aimanecouissi.animerestapi.controller;

import com.aimanecouissi.animerestapi.enums.MangaStatus;
import com.aimanecouissi.animerestapi.payload.dto.MangaDTO;
import com.aimanecouissi.animerestapi.payload.response.MangaPaginatedResponse;
import com.aimanecouissi.animerestapi.service.MangaService;
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
@RequestMapping("api/v1/manga")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Manga API", description = "Endpoints to manage manga resources.")
public class MangaController {
    private final MangaService mangaService;

    public MangaController(MangaService mangaService) {
        this.mangaService = mangaService;
    }

    @PostMapping
    @Operation(summary = "Create a new manga", description = "Endpoint to create a new manga entry.")
    @ApiResponse(responseCode = "201", description = "Manga successfully created.")
    public ResponseEntity<MangaDTO> createManga(@Valid @RequestBody MangaDTO mangaDTO) {
        MangaDTO createdManga = mangaService.createManga(mangaDTO);
        return new ResponseEntity<>(createdManga, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all manga", description = "Endpoint to retrieve a list of all manga entries.")
    @ApiResponse(responseCode = "200", description = "List of manga retrieved successfully.")
    public MangaPaginatedResponse getAllManga(
            @RequestParam(value = "pageNo", defaultValue = ApplicationConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = ApplicationConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = ApplicationConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = ApplicationConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir
    ) {
        return mangaService.getAllManga(pageNo, pageSize, sortBy, sortDir);
    }

    @GetMapping("{id}")
    @Operation(summary = "Get manga by ID", description = "Endpoint to retrieve details of a manga by its unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Manga details retrieved successfully."),
            @ApiResponse(responseCode = "404", description = "Manga not found.")
    })
    public ResponseEntity<MangaDTO> getMangaById(@PathVariable("id") long id) {
        MangaDTO manga = mangaService.getMangaById(id);
        return new ResponseEntity<>(manga, HttpStatus.OK);
    }

    @PutMapping("{id}")
    @Operation(summary = "Update manga by ID", description = "Endpoint to update details of an existing manga by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Manga updated successfully."),
            @ApiResponse(responseCode = "404", description = "Manga not found.")
    })
    public ResponseEntity<MangaDTO> updateManga(@PathVariable("id") long id, @Valid @RequestBody MangaDTO mangaDTO) {
        MangaDTO updatedManga = mangaService.updateManga(id, mangaDTO);
        return new ResponseEntity<>(updatedManga, HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Delete manga by ID", description = "Endpoint to delete a manga by its unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Manga deleted successfully."),
            @ApiResponse(responseCode = "404", description = "Manga not found.")
    })
    public ResponseEntity<String> deleteManga(@PathVariable("id") long id) {
        mangaService.deleteManga(id);
        return new ResponseEntity<>("The manga has been successfully deleted.", HttpStatus.OK);
    }

    @GetMapping("search")
    @Operation(summary = "Search manga by various attributes", description = "Endpoint to search for manga by various attributes.")
    @ApiResponse(responseCode = "200", description = "Manga list retrieved successfully.")
    public ResponseEntity<List<MangaDTO>> searchManga(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "status", required = false) MangaStatus status,
            @RequestParam(value = "rating", required = false) Integer rating,
            @RequestParam(value = "isFavorite", required = false) Boolean isFavorite
    ) {
        return ResponseEntity.ok(mangaService.searchManga(title, status, rating, isFavorite));
    }

    @GetMapping("/mean-rating")
    @Operation(summary = "Get mean rating of manga", description = "Endpoint to retrieve the mean rating of all manga entries for the current user.")
    @ApiResponse(responseCode = "200", description = "Mean rating retrieved successfully.")
    public ResponseEntity<Double> getMeanRating() {
        Double meanRating = mangaService.getMeanRating();
        return ResponseEntity.ok(meanRating);
    }
}
