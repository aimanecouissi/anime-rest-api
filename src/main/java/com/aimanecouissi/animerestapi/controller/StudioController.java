package com.aimanecouissi.animerestapi.controller;

import com.aimanecouissi.animerestapi.payload.dto.StudioDTO;
import com.aimanecouissi.animerestapi.service.StudioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/studios")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Studio API", description = "Endpoints to manage studio resources.")
public class StudioController {
    private final StudioService studioService;

    public StudioController(StudioService studioService) {
        this.studioService = studioService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new studio", description = "Endpoint to create a new studio.")
    @ApiResponse(responseCode = "201", description = "Studio successfully created.")
    public ResponseEntity<StudioDTO> createStudio(@Valid @RequestBody StudioDTO studioDTO) {
        StudioDTO createdStudio = studioService.createStudio(studioDTO);
        return new ResponseEntity<>(createdStudio, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all studios", description = "Endpoint to retrieve a list of all studios.")
    @ApiResponse(responseCode = "200", description = "List of studios retrieved successfully.")
    public ResponseEntity<List<StudioDTO>> getAllStudios() {
        List<StudioDTO> studios = studioService.getAllStudios();
        return ResponseEntity.ok(studios);
    }

    @GetMapping("{id}")
    @Operation(summary = "Get studio by ID", description = "Endpoint to retrieve details of a studio by its unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Studio details retrieved successfully."),
            @ApiResponse(responseCode = "404", description = "Studio not found.")
    })
    public ResponseEntity<StudioDTO> getStudioById(@PathVariable("id") long id) {
        StudioDTO studio = studioService.getStudioById(id);
        return new ResponseEntity<>(studio, HttpStatus.OK);
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update studio by ID", description = "Endpoint to update details of an existing studio by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Studio updated successfully."),
            @ApiResponse(responseCode = "404", description = "Studio not found.")
    })
    public ResponseEntity<StudioDTO> updateStudio(@PathVariable("id") long id, @Valid @RequestBody StudioDTO studioDTO) {
        StudioDTO updatedStudio = studioService.updateStudio(id, studioDTO);
        return new ResponseEntity<>(updatedStudio, HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete studio by ID", description = "Endpoint to delete a studio by its unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Studio deleted successfully."),
            @ApiResponse(responseCode = "404", description = "Studio not found.")
    })
    public ResponseEntity<String> deleteStudio(@PathVariable("id") long id) {
        studioService.deleteStudio(id);
        return new ResponseEntity<>("The studio has been successfully deleted.", HttpStatus.OK);
    }
}
