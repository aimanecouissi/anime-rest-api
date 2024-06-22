package com.aimanecouissi.animerestapi.controller;

import com.aimanecouissi.animerestapi.exception.ResourceNotFoundException;
import com.aimanecouissi.animerestapi.exception.UniqueFieldException;
import com.aimanecouissi.animerestapi.payload.dto.StudioDTO;
import com.aimanecouissi.animerestapi.security.JwtTokenProvider;
import com.aimanecouissi.animerestapi.service.StudioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StudioController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(OrderAnnotation.class)
public class StudioControllerTest {

    private static final String API_URL = "/api/v1/studios";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudioService studioService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private StudioDTO studioDTO;
    private StudioDTO createdStudio;

    @BeforeEach
    void setUp() {
        studioDTO = StudioDTO.builder()
                .name("Studio Ghibli")
                .build();

        createdStudio = StudioDTO.builder()
                .id(1L)
                .name("Studio Ghibli")
                .build();
    }

    private void performPostRequest(StudioDTO studioDTO, int expectedStatus) throws Exception {
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studioDTO)))
                .andExpect(status().is(expectedStatus));
    }

    private void performPutRequest(StudioDTO studioDTO, int expectedStatus) throws Exception {
        mockMvc.perform(put("/api/v1/studios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studioDTO)))
                .andExpect(status().is(expectedStatus));
    }

    private void performDeleteRequest() throws Exception {
        mockMvc.perform(delete("/api/v1/studios/1"))
                .andExpect(status().is(404));
    }

    @Test
    @Order(1)
    @DisplayName("Create Studio - Success")
    void shouldCreateStudio() throws Exception {
        when(studioService.createStudio(any(StudioDTO.class))).thenReturn(createdStudio);
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studioDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(createdStudio)));
    }

    @Test
    @Order(2)
    @DisplayName("Create Studio - Missing Name Field")
    void shouldThrowExceptionWhenCreatingStudioWithMissingNameField() throws Exception {
        StudioDTO invalidStudioDTO = StudioDTO.builder().build();
        performPostRequest(invalidStudioDTO, 400);
    }

    @Test
    @Order(3)
    @DisplayName("Create Studio - Duplicate Name")
    void shouldThrowExceptionWhenCreatingStudioWithDuplicateName() throws Exception {
        when(studioService.createStudio(any(StudioDTO.class))).thenThrow(new UniqueFieldException("Name", studioDTO.getName()));
        performPostRequest(studioDTO, 409);
    }

    @Test
    @Order(4)
    @DisplayName("Get All Studios - Success")
    void shouldGetAllStudios() throws Exception {
        List<StudioDTO> studios = Arrays.asList(
                StudioDTO.builder().id(1L).name("Studio Ghibli").build(),
                StudioDTO.builder().id(2L).name("Toei Animation").build()
        );
        when(studioService.getAllStudios()).thenReturn(studios);
        mockMvc.perform(get(API_URL))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(studios)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(studios.size())));
    }

    @Test
    @Order(5)
    @DisplayName("Get Studio By ID - Success")
    void shouldGetStudioById() throws Exception {
        when(studioService.getStudioById(anyLong())).thenReturn(createdStudio);
        mockMvc.perform(get(API_URL + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(createdStudio)));
    }

    @Test
    @Order(6)
    @DisplayName("Get Studio By ID - Not Found")
    void shouldThrowExceptionWhenGettingStudioByIdNotFound() throws Exception {
        when(studioService.getStudioById(anyLong())).thenThrow(new ResourceNotFoundException("Studio", "ID", "1"));
        mockMvc.perform(get(API_URL + "/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(7)
    @DisplayName("Update Studio - Success")
    void shouldUpdateStudio() throws Exception {
        StudioDTO updatedStudio = StudioDTO.builder()
                .id(1L)
                .name("Studio Pierrot")
                .build();
        when(studioService.updateStudio(anyLong(), any(StudioDTO.class))).thenReturn(updatedStudio);
        mockMvc.perform(put(API_URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studioDTO)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(updatedStudio)));
    }

    @Test
    @Order(8)
    @DisplayName("Update Studio - Not Found")
    void shouldThrowExceptionWhenUpdatingStudioNotFound() throws Exception {
        when(studioService.updateStudio(anyLong(), any(StudioDTO.class))).thenThrow(new ResourceNotFoundException("Studio", "ID", "1"));
        performPutRequest(studioDTO, 404);
    }

    @Test
    @Order(9)
    @DisplayName("Update Studio - Duplicate Name")
    void shouldThrowExceptionWhenUpdatingStudioWithDuplicateName() throws Exception {
        StudioDTO duplicateNameStudioDTO = StudioDTO.builder()
                .name("Duplicate Studio")
                .build();
        when(studioService.updateStudio(anyLong(), any(StudioDTO.class))).thenThrow(new UniqueFieldException("Name", duplicateNameStudioDTO.getName()));
        performPutRequest(duplicateNameStudioDTO, 409);
    }

    @Test
    @Order(10)
    @DisplayName("Delete Studio - Success")
    void shouldDeleteStudio() throws Exception {
        doNothing().when(studioService).deleteStudio(anyLong());
        mockMvc.perform(delete(API_URL + "/{id}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    @Order(11)
    @DisplayName("Delete Studio - Not Found")
    void shouldThrowExceptionWhenDeletingStudioNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Studio", "ID", "1")).when(studioService).deleteStudio(anyLong());
        performDeleteRequest();
    }
}
