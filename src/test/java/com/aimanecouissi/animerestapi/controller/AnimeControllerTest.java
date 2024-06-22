package com.aimanecouissi.animerestapi.controller;

import com.aimanecouissi.animerestapi.enums.AnimeStatus;
import com.aimanecouissi.animerestapi.enums.AnimeType;
import com.aimanecouissi.animerestapi.exception.ResourceNotFoundException;
import com.aimanecouissi.animerestapi.exception.UniqueFieldException;
import com.aimanecouissi.animerestapi.payload.dto.AnimeDTO;
import com.aimanecouissi.animerestapi.payload.response.AnimePaginatedResponse;
import com.aimanecouissi.animerestapi.security.JwtTokenProvider;
import com.aimanecouissi.animerestapi.service.AnimeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AnimeController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(OrderAnnotation.class)
public class AnimeControllerTest {

    private static final String API_URL = "/api/v1/anime";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnimeService animeService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private AnimeDTO animeDTO;
    private AnimeDTO createdAnime;

    @BeforeEach
    void setUp() {
        animeDTO = AnimeDTO.builder()
                .title("Spirited Away")
                .type(AnimeType.MOVIE)
                .status(AnimeStatus.COMPLETED)
                .rating(10)
                .isFavorite(true)
                .isComplete(true)
                .studioId(1L)
                .build();

        createdAnime = AnimeDTO.builder()
                .id(1L)
                .title("Spirited Away")
                .type(AnimeType.MOVIE)
                .status(AnimeStatus.COMPLETED)
                .rating(10)
                .isFavorite(true)
                .isComplete(true)
                .studioId(1L)
                .build();
    }

    private void performPostRequest(Object dto, int expectedStatus) throws Exception {
        mockMvc.perform(post(API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        ).andExpect(status().is(expectedStatus));
    }

    private void performGetRequest() throws Exception {
        mockMvc.perform(get("/api/v1/anime/1"))
                .andExpect(status().is(404));
    }

    private void performPutRequest(Object dto, int expectedStatus) throws Exception {
        mockMvc.perform(put("/api/v1/anime/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        ).andExpect(status().is(expectedStatus));
    }

    private void performDeleteRequest() throws Exception {
        mockMvc.perform(delete("/api/v1/anime/1"))
                .andExpect(status().is(404));
    }

    @Test
    @Order(1)
    @DisplayName("Create Anime - Success")
    void shouldCreateAnime() throws Exception {
        when(animeService.createAnime(any(AnimeDTO.class))).thenReturn(createdAnime);
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(animeDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(createdAnime)));
    }

    @Test
    @Order(2)
    @DisplayName("Create Anime - Missing Fields")
    void shouldThrowExceptionWhenCreatingAnimeWithMissingFields() throws Exception {
        AnimeDTO invalidAnimeDTO = AnimeDTO.builder().build();
        performPostRequest(invalidAnimeDTO, 400);
    }

    @Test
    @Order(3)
    @DisplayName("Create Anime - Duplicate Title")
    void shouldThrowExceptionWhenCreatingAnimeWithDuplicateTitle() throws Exception {
        when(animeService.createAnime(any(AnimeDTO.class))).thenThrow(new UniqueFieldException("Title", animeDTO.getTitle()));
        performPostRequest(animeDTO, 409);
    }

    @Test
    @Order(4)
    @DisplayName("Get All Anime - Success")
    void shouldGetAllAnime() throws Exception {
        AnimePaginatedResponse paginatedResponse = AnimePaginatedResponse.builder()
                .items(Collections.singletonList(AnimeDTO.builder()
                                .id(1L)
                                .title("Jujutsu Kaisen")
                                .type(AnimeType.TV)
                                .studioId(1L)
                                .status(AnimeStatus.WATCHING)
                                .rating(5)
                                .isFavorite(false)
                                .isComplete(false)
                                .build()))
                .pageNumber(1)
                .pageSize(10)
                .totalPages(1)
                .totalElements(1)
                .isLast(true)
                .build();
        when(animeService.getAllAnime(anyInt(), anyInt(), anyString(), anyString())).thenReturn(paginatedResponse);
        mockMvc.perform(get(API_URL))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(paginatedResponse)));
    }

    @Test
    @Order(5)
    @DisplayName("Get Anime By ID - Success")
    void shouldGetAnimeById() throws Exception {
        when(animeService.getAnimeById(anyLong())).thenReturn(createdAnime);
        mockMvc.perform(get(API_URL + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(createdAnime)));
    }

    @Test
    @Order(6)
    @DisplayName("Get Anime By ID - Not Found")
    void shouldThrowExceptionWhenAnimeNotFoundById() throws Exception {
        when(animeService.getAnimeById(anyLong())).thenThrow(new ResourceNotFoundException("Anime", "ID", "1"));
        performGetRequest();
    }

    @Test
    @Order(7)
    @DisplayName("Update Anime - Success")
    void shouldUpdateAnime() throws Exception {
        AnimeDTO updatedAnime = AnimeDTO.builder()
                .id(1L)
                .title("Attack on Titan")
                .type(AnimeType.TV)
                .status(AnimeStatus.COMPLETED)
                .rating(10)
                .isFavorite(true)
                .isComplete(false)
                .studioId(1L)
                .build();
        when(animeService.updateAnime(anyLong(), any(AnimeDTO.class))).thenReturn(updatedAnime);
        mockMvc.perform(put(API_URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(animeDTO)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(updatedAnime)));
    }

    @Test
    @Order(8)
    @DisplayName("Update Anime - Not Found")
    void shouldThrowExceptionWhenUpdatingAnimeNotFound() throws Exception {
        when(animeService.updateAnime(anyLong(), any(AnimeDTO.class))).thenThrow(new ResourceNotFoundException("Anime", "ID", "1"));
        performPutRequest(animeDTO, 404);
    }

    @Test
    @Order(9)
    @DisplayName("Update Anime - Duplicate Title")
    void shouldThrowExceptionWhenUpdatingAnimeWithDuplicateTitle() throws Exception {
        when(animeService.updateAnime(anyLong(), any(AnimeDTO.class))).thenThrow(new UniqueFieldException("Title", animeDTO.getTitle()));
        performPutRequest(animeDTO, 409);
    }

    @Test
    @Order(10)
    @DisplayName("Delete Anime - Success")
    void shouldDeleteAnime() throws Exception {
        mockMvc.perform(delete(API_URL + "/{id}", 1L)).andExpect(status().isOk());
    }

    @Test
    @Order(11)
    @DisplayName("Delete Anime - Not Found")
    void shouldThrowExceptionWhenDeletingAnimeNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Anime", "ID", "1")).when(animeService).deleteAnime(anyLong());
        performDeleteRequest();
    }

    @Test
    @Order(12)
    @DisplayName("Search Anime - Success")
    void shouldSearchAnime() throws Exception {
        List<AnimeDTO> animeList = Collections.singletonList(
                AnimeDTO.builder()
                        .id(1L)
                        .title("Attack on Titan")
                        .type(AnimeType.TV)
                        .status(AnimeStatus.COMPLETED)
                        .rating(10)
                        .isFavorite(true)
                        .isComplete(false)
                        .studioId(1L)
                        .build()
        );
        when(animeService.searchAnime(
                anyString(),
                any(AnimeType.class),
                any(AnimeStatus.class),
                anyInt(),
                anyBoolean(),
                anyBoolean()
        )).thenReturn(animeList);
        mockMvc.perform(get(API_URL + "/search")
                        .param("title", "My Neighbor Totoro")
                        .param("type", AnimeType.MOVIE.toString())
                        .param("status", AnimeStatus.WATCHING.toString())
                        .param("rating", "8")
                        .param("isFavorite", "true")
                        .param("isComplete", "false"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(animeList)));
    }

    @Test
    @Order(13)
    @DisplayName("Get Mean Rating - Success")
    void shouldGetMeanRating() throws Exception {
        Double meanRating = 8.5;
        when(animeService.getMeanRating()).thenReturn(meanRating);
        mockMvc.perform(get(API_URL + "/mean-rating"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(meanRating)));
    }
}
