package com.aimanecouissi.animerestapi.controller;

import com.aimanecouissi.animerestapi.enums.MangaStatus;
import com.aimanecouissi.animerestapi.exception.ResourceNotFoundException;
import com.aimanecouissi.animerestapi.exception.UniqueFieldException;
import com.aimanecouissi.animerestapi.payload.dto.MangaDTO;
import com.aimanecouissi.animerestapi.payload.response.MangaPaginatedResponse;
import com.aimanecouissi.animerestapi.security.JwtTokenProvider;
import com.aimanecouissi.animerestapi.service.MangaService;
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

@WebMvcTest(controllers = MangaController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(OrderAnnotation.class)
public class MangaControllerTest {

    private static final String API_URL = "/api/v1/manga";

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private MangaService mangaService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private MangaDTO mangaDTO;
    private MangaDTO createdManga;

    @BeforeEach
    void setUp() {
        mangaDTO = MangaDTO.builder()
                .title("Naruto")
                .status(MangaStatus.COMPLETED)
                .rating(9)
                .isFavorite(true)
                .build();

        createdManga = MangaDTO.builder()
                .id(1L)
                .title("Naruto")
                .status(MangaStatus.COMPLETED)
                .rating(9)
                .isFavorite(true)
                .build();
    }

    private void performPostRequest(Object dto, int expectedStatus) throws Exception {
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is(expectedStatus));
    }

    private void performGetRequest() throws Exception {
        mockMvc.perform(get("/api/v1/manga/1"))
                .andExpect(status().is(404));
    }

    private void performPutRequest(Object dto, int expectedStatus) throws Exception {
        mockMvc.perform(put("/api/v1/manga/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is(expectedStatus));
    }

    private void performDeleteRequest() throws Exception {
        mockMvc.perform(delete("/api/v1/manga/1"))
                .andExpect(status().is(404));
    }

    @Test
    @Order(1)
    @DisplayName("Create Manga - Success")
    void shouldCreateManga() throws Exception {
        when(mangaService.createManga(any(MangaDTO.class))).thenReturn(createdManga);
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mangaDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(createdManga)));
    }

    @Test
    @Order(2)
    @DisplayName("Create Manga - Missing Fields")
    void shouldThrowExceptionWhenCreatingMangaWithMissingFields() throws Exception {
        MangaDTO invalidMangaDTO = MangaDTO.builder().build();
        performPostRequest(invalidMangaDTO, 400);
    }

    @Test
    @Order(3)
    @DisplayName("Create Manga - Duplicate Title")
    void shouldThrowExceptionWhenCreatingMangaWithDuplicateTitle() throws Exception {
        when(mangaService.createManga(any(MangaDTO.class))).thenThrow(new UniqueFieldException("Title", mangaDTO.getTitle()));
        performPostRequest(mangaDTO, 409);
    }

    @Test
    @Order(4)
    @DisplayName("Get All Manga - Success")
    void shouldGetAllManga() throws Exception {
        MangaPaginatedResponse paginatedResponse = MangaPaginatedResponse.builder()
                .items(Collections.singletonList(MangaDTO.builder()
                        .id(1L)
                        .title("One Piece")
                        .status(MangaStatus.READING)
                        .rating(10)
                        .isFavorite(true)
                        .build()))
                .pageNumber(1)
                .pageSize(10)
                .totalPages(1)
                .totalElements(1)
                .isLast(true)
                .build();
        when(mangaService.getAllManga(anyInt(), anyInt(), anyString(), anyString())).thenReturn(paginatedResponse);
        mockMvc.perform(get(API_URL))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(paginatedResponse)));
    }

    @Test
    @Order(5)
    @DisplayName("Get Manga By ID - Success")
    void shouldGetMangaById() throws Exception {
        when(mangaService.getMangaById(anyLong())).thenReturn(createdManga);
        mockMvc.perform(get(API_URL + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(createdManga)));
    }

    @Test
    @Order(6)
    @DisplayName("Get Manga By ID - Not Found")
    void shouldThrowExceptionWhenMangaNotFoundById() throws Exception {
        when(mangaService.getMangaById(anyLong())).thenThrow(new ResourceNotFoundException("Manga", "ID", "1"));
        performGetRequest();
    }

    @Test
    @Order(7)
    @DisplayName("Update Manga - Success")
    void shouldUpdateManga() throws Exception {
        MangaDTO updatedManga = MangaDTO.builder()
                .id(1L)
                .title("Naruto Shippuden")
                .status(MangaStatus.COMPLETED)
                .rating(9)
                .isFavorite(true)
                .build();
        when(mangaService.updateManga(anyLong(), any(MangaDTO.class))).thenReturn(updatedManga);
        mockMvc.perform(put(API_URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mangaDTO)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(updatedManga)));
    }

    @Test
    @Order(8)
    @DisplayName("Update Manga - Not Found")
    void shouldThrowExceptionWhenUpdatingMangaNotFound() throws Exception {
        when(mangaService.updateManga(anyLong(), any(MangaDTO.class))).thenThrow(new ResourceNotFoundException("Manga", "ID", "1"));
        performPutRequest(mangaDTO, 404);
    }

    @Test
    @Order(9)
    @DisplayName("Update Manga - Duplicate Title")
    void shouldThrowExceptionWhenUpdatingMangaWithDuplicateTitle() throws Exception {
        MangaDTO duplicateTitleMangaDTO = MangaDTO.builder()
                .title("One Piece")
                .status(MangaStatus.READING)
                .rating(9)
                .isFavorite(true)
                .build();
        when(mangaService.updateManga(anyLong(), any(MangaDTO.class))).thenThrow(new UniqueFieldException("Title", duplicateTitleMangaDTO.getTitle()));
        performPutRequest(duplicateTitleMangaDTO, 409);
    }

    @Test
    @Order(10)
    @DisplayName("Delete Manga - Success")
    void shouldDeleteManga() throws Exception {
        mockMvc.perform(delete(API_URL + "/{id}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    @Order(11)
    @DisplayName("Delete Manga - Not Found")
    void shouldThrowExceptionWhenDeletingMangaNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Manga", "ID", "1")).when(mangaService).deleteManga(anyLong());
        performDeleteRequest();
    }

    @Test
    @Order(12)
    @DisplayName("Search Manga - Success")
    void shouldSearchManga() throws Exception {
        List<MangaDTO> mangaList = Collections.singletonList(MangaDTO.builder()
                .id(1L)
                .title("Naruto")
                .status(MangaStatus.COMPLETED)
                .rating(9)
                .isFavorite(true)
                .build());
        when(mangaService.searchManga(anyString(), any(MangaStatus.class), anyInt(), anyBoolean())).thenReturn(mangaList);
        mockMvc.perform(get(API_URL + "/search")
                        .param("title", "Naruto")
                        .param("status", MangaStatus.COMPLETED.toString())
                        .param("rating", "9")
                        .param("isFavorite", "true"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(mangaList)));
    }

    @Test
    @Order(13)
    @DisplayName("Get Mean Rating - Success")
    void shouldGetMeanRating() throws Exception {
        Double meanRating = 8.5;
        when(mangaService.getMeanRating()).thenReturn(meanRating);
        mockMvc.perform(get(API_URL + "/mean-rating"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(meanRating)));
    }
}
