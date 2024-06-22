package com.aimanecouissi.animerestapi.integration;

import com.aimanecouissi.animerestapi.entity.Manga;
import com.aimanecouissi.animerestapi.entity.Role;
import com.aimanecouissi.animerestapi.entity.User;
import com.aimanecouissi.animerestapi.enums.MangaStatus;
import com.aimanecouissi.animerestapi.payload.dto.MangaDTO;
import com.aimanecouissi.animerestapi.repository.MangaRepository;
import com.aimanecouissi.animerestapi.repository.RoleRepository;
import com.aimanecouissi.animerestapi.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(OrderAnnotation.class)
public class MangaControllerIntegrationTest {

    private static final String API_URL = "/api/v1/manga";
    private static User user;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MangaRepository mangaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MangaDTO mangaDTO;
    private Manga manga;

    @BeforeAll
    static void init(
            @Autowired UserRepository userRepository,
            @Autowired RoleRepository roleRepository,
            @Autowired MangaRepository mangaRepository
    ) {
        mangaRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create an admin role for user
        Role roleAdmin = Role.builder().name("ROLE_ADMIN").build();
        roleRepository.save(roleAdmin);

        // Create a user role for user
        Role roleUser = Role.builder().name("ROLE_USER").build();
        roleRepository.save(roleUser);

        // Create a user for manga
        user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .username("john.doe")
                .password("password123")
                .roles(Set.of(roleAdmin, roleUser))
                .build();
        userRepository.save(user);
    }

    @BeforeEach
    void setUp() {
        mangaRepository.deleteAll();

        // Manga DTO
        mangaDTO = MangaDTO.builder()
                .title("Naruto")
                .status(MangaStatus.COMPLETED)
                .rating(9)
                .isFavorite(true)
                .build();

        // Manga BO
        manga = Manga.builder()
                .title("Naruto")
                .status(MangaStatus.COMPLETED)
                .rating(9)
                .isFavorite(true)
                .user(user)
                .build();
    }

    private void performPostRequest(MangaDTO mangaDTO, int expectedStatus) throws Exception {
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mangaDTO)))
                .andExpect(status().is(expectedStatus));
    }

    private void performPutRequest(MangaDTO mangaDTO, long id, int expectedStatus) throws Exception {
        mockMvc.perform(put(API_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mangaDTO)))
                .andExpect(status().is(expectedStatus));
    }

    private void performDeleteRequest() throws Exception {
        mockMvc.perform(delete(API_URL + "/{id}", (long) 999))
                .andExpect(status().is(404));
    }

    @Test
    @Order(1)
    @DisplayName("Create Manga - Success")
    @WithMockUser(username = "john.doe", roles = {"ADMIN"})
    void shouldCreateManga() throws Exception {
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mangaDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is(mangaDTO.getTitle())))
                .andExpect(jsonPath("$.status", is(mangaDTO.getStatus().toString())))
                .andExpect(jsonPath("$.rating", is(mangaDTO.getRating())));
    }

    @Test
    @Order(2)
    @DisplayName("Create Manga - Missing Fields")
    @WithMockUser(username = "john.doe", roles = {"ADMIN"})
    void shouldThrowExceptionWhenCreatingMangaWithMissingFields() throws Exception {
        MangaDTO invalidMangaDTO = MangaDTO.builder().build();
        performPostRequest(invalidMangaDTO, 400);
    }

    @Test
    @Order(3)
    @DisplayName("Create Manga - Duplicate Title")
    @WithMockUser(username = "john.doe", roles = {"ADMIN"})
    void shouldThrowExceptionWhenCreatingMangaWithDuplicateTitle() throws Exception {
        mangaRepository.save(manga);
        performPostRequest(mangaDTO, 409);
    }

    @Test
    @Order(4)
    @DisplayName("Get All Manga - Success")
    @WithMockUser(username = "john.doe")
    void shouldGetAllManga() throws Exception {
        List<Manga> mangas = Collections.singletonList(
                Manga.builder()
                        .title("One Piece")
                        .status(MangaStatus.READING)
                        .rating(10)
                        .isFavorite(true)
                        .user(user)
                        .build()
        );
        mangaRepository.saveAll(mangas);
        mockMvc.perform(get(API_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.size()", is(1)))
                .andExpect(jsonPath("$.items[0].title", is("One Piece")))
                .andExpect(jsonPath("$.items[0].status", is(MangaStatus.READING.toString())))
                .andExpect(jsonPath("$.items[0].rating", is(10)))
                .andExpect(jsonPath("$.items[0].favorite", is(true)));
    }

    @Test
    @Order(5)
    @DisplayName("Get Manga By ID - Success")
    @WithMockUser(username = "john.doe")
    void shouldGetMangaById() throws Exception {
        mangaRepository.save(manga);
        mockMvc.perform(get(API_URL + "/{id}", manga.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(manga.getTitle())))
                .andExpect(jsonPath("$.status", is(manga.getStatus().toString())))
                .andExpect(jsonPath("$.rating", is(manga.getRating())));
    }

    @Test
    @Order(6)
    @DisplayName("Get Manga By ID - Not Found")
    @WithMockUser(username = "john.doe")
    void shouldThrowExceptionWhenMangaNotFoundById() throws Exception {
        mockMvc.perform(get(API_URL + "/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(7)
    @DisplayName("Update Manga - Success")
    @WithMockUser(username = "john.doe", roles = {"ADMIN"})
    void shouldUpdateManga() throws Exception {
        mangaRepository.save(manga);
        MangaDTO updatedManga = MangaDTO.builder()
                .title("Naruto Shippuden")
                .status(MangaStatus.COMPLETED)
                .rating(9)
                .isFavorite(true)
                .build();
        mockMvc.perform(put(API_URL + "/{id}", manga.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedManga)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(updatedManga.getTitle())))
                .andExpect(jsonPath("$.status", is(updatedManga.getStatus().toString())))
                .andExpect(jsonPath("$.rating", is(updatedManga.getRating())));
    }

    @Test
    @Order(8)
    @DisplayName("Update Manga - Not Found")
    @WithMockUser(username = "john.doe", roles = {"ADMIN"})
    void shouldThrowExceptionWhenUpdatingMangaNotFound() throws Exception {
        MangaDTO updatedManga = MangaDTO.builder()
                .title("Naruto Shippuden")
                .status(MangaStatus.COMPLETED)
                .rating(9)
                .isFavorite(true)
                .build();
        performPutRequest(updatedManga, 999, 404);
    }

    @Test
    @Order(9)
    @DisplayName("Update Manga - Duplicate Title")
    @WithMockUser(username = "john.doe", roles = {"ADMIN"})
    void shouldThrowExceptionWhenUpdatingMangaWithDuplicateTitle() throws Exception {
        Manga manga1 = Manga.builder()
                .title("Naruto")
                .status(MangaStatus.COMPLETED)
                .rating(9)
                .isFavorite(true)
                .user(user)
                .build();
        mangaRepository.save(manga1);
        Manga manga2 = Manga.builder()
                .title("One Piece")
                .status(MangaStatus.READING)
                .rating(10)
                .isFavorite(true)
                .user(user)
                .build();
        mangaRepository.save(manga2);
        MangaDTO updatedManga = MangaDTO.builder()
                .title("One Piece")
                .status(MangaStatus.READING)
                .rating(10)
                .isFavorite(true)
                .build();
        performPutRequest(updatedManga, manga1.getId(), 409);
    }

    @Test
    @Order(10)
    @DisplayName("Delete Manga - Success")
    @WithMockUser(username = "john.doe", roles = {"ADMIN"})
    void shouldDeleteManga() throws Exception {
        mangaRepository.save(manga);
        mockMvc.perform(delete(API_URL + "/{id}", manga.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @Order(11)
    @DisplayName("Delete Manga - Not Found")
    @WithMockUser(username = "john.doe", roles = {"ADMIN"})
    void shouldThrowExceptionWhenDeletingMangaNotFound() throws Exception {
        performDeleteRequest();
    }

    @Test
    @Order(12)
    @DisplayName("Search Manga - Success")
    @WithMockUser(username = "john.doe")
    void shouldSearchManga() throws Exception {
        mangaRepository.save(manga);

        mockMvc.perform(get(API_URL + "/search")
                        .param("title", "Naruto")
                        .param("status", MangaStatus.COMPLETED.toString())
                        .param("rating", "9")
                        .param("isFavorite", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].title", is("Naruto")))
                .andExpect(jsonPath("$[0].status", is(MangaStatus.COMPLETED.toString())))
                .andExpect(jsonPath("$[0].rating", is(9)))
                .andExpect(jsonPath("$[0].favorite", is(true)));
    }

    @Test
    @Order(13)
    @DisplayName("Get Mean Rating - Success")
    @WithMockUser(username = "john.doe")
    void shouldGetMeanRating() throws Exception {
        Manga manga1 = Manga.builder()
                .title("Naruto")
                .status(MangaStatus.COMPLETED)
                .rating(9)
                .isFavorite(true)
                .user(user)
                .build();
        mangaRepository.save(manga1);
        Manga manga2 = Manga.builder()
                .title("One Piece")
                .status(MangaStatus.READING)
                .rating(8)
                .isFavorite(false)
                .user(user)
                .build();
        mangaRepository.save(manga2);
        Double meanRating = (manga1.getRating() + manga2.getRating()) / 2.0;
        mockMvc.perform(get(API_URL + "/mean-rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(meanRating)));
    }
}
