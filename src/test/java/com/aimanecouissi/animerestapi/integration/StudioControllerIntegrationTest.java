package com.aimanecouissi.animerestapi.integration;

import com.aimanecouissi.animerestapi.entity.Studio;
import com.aimanecouissi.animerestapi.payload.dto.StudioDTO;
import com.aimanecouissi.animerestapi.repository.StudioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(OrderAnnotation.class)
public class StudioControllerIntegrationTest {

    private static final String API_URL = "/api/v1/studios";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudioRepository studioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private StudioDTO studioDTO;
    private Studio studio;

    @BeforeEach
    void setUp() {
        studioRepository.deleteAll();

        // Studio DTO
        studioDTO = StudioDTO.builder()
                .name("Studio Ghibli")
                .build();

        // Studio BO
        studio = Studio.builder()
                .name("Studio Ghibli")
                .build();
    }

    private void performPostRequest(StudioDTO studioDTO, int expectedStatus) throws Exception {
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studioDTO)))
                .andExpect(status().is(expectedStatus));
    }

    private void performPutRequest(StudioDTO studioDTO, long id, int expectedStatus) throws Exception {
        mockMvc.perform(put(API_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studioDTO)))
                .andExpect(status().is(expectedStatus));
    }

    private void performDeleteRequest(long id) throws Exception {
        mockMvc.perform(delete(API_URL + "/{id}", id))
                .andExpect(status().is(404));
    }

    @Test
    @Order(1)
    @DisplayName("Create Studio - Success")
    @WithMockUser(username = "aimanecouissi", roles = {"ADMIN"})
    void shouldCreateStudio() throws Exception {
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studioDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(studioDTO.getName()));
    }

    @Test
    @Order(2)
    @DisplayName("Create Studio - Missing Name Field")
    @WithMockUser(username = "aimanecouissi", roles = {"ADMIN"})
    void shouldThrowExceptionWhenCreatingStudioWithMissingNameField() throws Exception {
        StudioDTO invalidStudioDTO = StudioDTO.builder().build();
        performPostRequest(invalidStudioDTO, 400);
    }

    @Test
    @Order(3)
    @DisplayName("Create Studio - Duplicate Name")
    @WithMockUser(username = "aimanecouissi", roles = {"ADMIN"})
    void shouldThrowExceptionWhenCreatingStudioWithDuplicateName() throws Exception {
        studioRepository.save(studio);
        performPostRequest(studioDTO, 409);
    }

    @Test
    @Order(4)
    @DisplayName("Get All Studios - Success")
    @WithMockUser(username = "couissiaimane")
    void shouldGetAllStudios() throws Exception {
        List<Studio> studios = Arrays.asList(
                Studio.builder().name("Studio Ghibli").build(),
                Studio.builder().name("Toei Animation").build()
        );
        studioRepository.saveAll(studios);
        mockMvc.perform(get(API_URL))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(2));
    }

    @Test
    @Order(5)
    @DisplayName("Get Studio By ID - Success")
    @WithMockUser(username = "couissiaimane")
    void shouldGetStudioById() throws Exception {
        studioRepository.save(studio);
        mockMvc.perform(get(API_URL + "/{id}", studio.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(studio.getName()));
    }

    @Test
    @Order(6)
    @DisplayName("Get Studio By ID - Not Found")
    @WithMockUser(username = "couissiaimane")
    void shouldThrowExceptionWhenGettingStudioByIdNotFound() throws Exception {
        studioRepository.save(studio);
        mockMvc.perform(get(API_URL + "/{id}", studio.getId() + 1))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(7)
    @DisplayName("Update Studio - Success")
    @WithMockUser(username = "aimanecouissi", roles = {"ADMIN"})
    void shouldUpdateStudio() throws Exception {
        studioRepository.save(studio);
        StudioDTO updatedStudio = StudioDTO.builder()
                .name("Studio Pierrot")
                .build();
        mockMvc.perform(put(API_URL + "/{id}", studio.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedStudio)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updatedStudio.getName()));
    }

    @Test
    @Order(8)
    @DisplayName("Update Studio - Not Found")
    @WithMockUser(username = "aimanecouissi", roles = {"ADMIN"})
    void shouldThrowExceptionWhenUpdatingStudioNotFound() throws Exception {
        studioRepository.save(studio);
        StudioDTO updatedStudio = StudioDTO.builder()
                .name("Studio Pierrot")
                .build();
        performPutRequest(updatedStudio, studio.getId() + 1, 404);
    }

    @Test
    @Order(9)
    @DisplayName("Update Studio - Duplicate Name")
    @WithMockUser(username = "aimanecouissi", roles = {"ADMIN"})
    void shouldThrowExceptionWhenUpdatingStudioWithDuplicateName() throws Exception {
        Studio studio1 = Studio.builder()
                .name("Studio Ghibli")
                .build();
        studioRepository.save(studio1);
        Studio studio2 = Studio.builder()
                .name("Studio Madhouse")
                .build();
        studioRepository.save(studio2);
        StudioDTO updatedStudio = StudioDTO.builder()
                .name("Studio Madhouse")
                .build();
        performPutRequest(updatedStudio, studio1.getId(), 409);
    }

    @Test
    @Order(10)
    @DisplayName("Delete Studio - Success")
    @WithMockUser(username = "aimanecouissi", roles = {"ADMIN"})
    void shouldDeleteStudio() throws Exception {
        studioRepository.save(studio);
        mockMvc.perform(delete(API_URL + "/{id}", studio.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @Order(11)
    @DisplayName("Delete Studio - Not Found")
    @WithMockUser(username = "aimanecouissi", roles = {"ADMIN"})
    void shouldThrowExceptionWhenDeletingStudioNotFound() throws Exception {
        studioRepository.save(studio);
        performDeleteRequest(studio.getId() + 1);
    }
}
