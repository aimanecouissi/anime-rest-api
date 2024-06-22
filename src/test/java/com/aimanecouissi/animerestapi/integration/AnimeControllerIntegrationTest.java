package com.aimanecouissi.animerestapi.integration;

import com.aimanecouissi.animerestapi.entity.Anime;
import com.aimanecouissi.animerestapi.entity.Role;
import com.aimanecouissi.animerestapi.entity.Studio;
import com.aimanecouissi.animerestapi.entity.User;
import com.aimanecouissi.animerestapi.enums.AnimeStatus;
import com.aimanecouissi.animerestapi.enums.AnimeType;
import com.aimanecouissi.animerestapi.payload.dto.AnimeDTO;
import com.aimanecouissi.animerestapi.repository.AnimeRepository;
import com.aimanecouissi.animerestapi.repository.RoleRepository;
import com.aimanecouissi.animerestapi.repository.StudioRepository;
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
public class AnimeControllerIntegrationTest {

    private static final String API_URL = "/api/v1/anime";
    private static User user;
    private static Studio studio;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AnimeRepository animeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private AnimeDTO animeDTO;
    private Anime anime;

    @BeforeAll
    static void init(
            @Autowired UserRepository userRepository,
            @Autowired RoleRepository roleRepository,
            @Autowired StudioRepository studioRepository
    ) {
        studioRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create a Role for user
        Role role = Role.builder().name("ROLE_USER").build();
        roleRepository.save(role);

        // Create a user for anime
        user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .username("john.doe")
                .password("password123")
                .roles(Set.of(role))
                .build();
        userRepository.save(user);

        // Create a studio for anime
        studio = Studio.builder().name("Studio Ghibli").build();
        studioRepository.save(studio);
    }

    @BeforeEach
    void setUp() {
        animeRepository.deleteAll();

        // Anime DTO
        animeDTO = AnimeDTO.builder()
                .title("Spirited Away")
                .type(AnimeType.MOVIE)
                .status(AnimeStatus.COMPLETED)
                .rating(10)
                .isFavorite(true)
                .isComplete(true)
                .studioId(studio.getId())
                .build();

        // Anime BO
        anime = Anime.builder()
                .title("Spirited Away")
                .type(AnimeType.MOVIE)
                .status(AnimeStatus.COMPLETED)
                .rating(10)
                .isFavorite(true)
                .isComplete(true)
                .studio(studio)
                .user(user)
                .build();
    }

    private void performPostRequest(AnimeDTO animeDTO, int expectedStatus) throws Exception {
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(animeDTO)))
                .andExpect(status().is(expectedStatus));
    }

    private void performPutRequest(AnimeDTO animeDTO, long id, int expectedStatus) throws Exception {
        mockMvc.perform(put(API_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(animeDTO)))
                .andExpect(status().is(expectedStatus));
    }

    private void performDeleteRequest() throws Exception {
        mockMvc.perform(delete(API_URL + "/{id}", (long) 999))
                .andExpect(status().is(404));
    }

    @Test
    @Order(1)
    @DisplayName("Create Anime - Success")
    @WithMockUser(username = "john.doe")
    void shouldCreateAnime() throws Exception {
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(animeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is(animeDTO.getTitle())))
                .andExpect(jsonPath("$.type", is(animeDTO.getType().toString())))
                .andExpect(jsonPath("$.status", is(animeDTO.getStatus().toString())))
                .andExpect(jsonPath("$.rating", is(animeDTO.getRating())))
                .andExpect(jsonPath("$.favorite", is(animeDTO.isFavorite())))
                .andExpect(jsonPath("$.complete", is(animeDTO.isComplete())))
                .andExpect(jsonPath("$.studioId", is(animeDTO.getStudioId().intValue())));
    }

    @Test
    @Order(2)
    @DisplayName("Create Anime - Missing Fields")
    @WithMockUser(username = "john.doe")
    void shouldThrowExceptionWhenCreatingAnimeWithMissingFields() throws Exception {
        AnimeDTO invalidAnimeDTO = AnimeDTO.builder().build();
        performPostRequest(invalidAnimeDTO, 400);
    }

    @Test
    @Order(3)
    @DisplayName("Create Anime - Duplicate Title")
    @WithMockUser(username = "john.doe")
    void shouldThrowExceptionWhenCreatingAnimeWithDuplicateTitle() throws Exception {
        animeRepository.save(anime);
        performPostRequest(animeDTO, 409);
    }

    @Test
    @Order(4)
    @DisplayName("Get All Anime - Success")
    @WithMockUser(username = "john.doe")
    void shouldGetAllAnime() throws Exception {
        List<Anime> animeList = Collections.singletonList(Anime.builder()
                .title("My Neighbor Totoro")
                .type(AnimeType.TV)
                .status(AnimeStatus.WATCHING)
                .rating(5)
                .isFavorite(false)
                .isComplete(false)
                .studio(studio)
                .user(user)
                .build()
        );
        animeRepository.saveAll(animeList);
        mockMvc.perform(get(API_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.size()", is(1)))
                .andExpect(jsonPath("$.items[0].title", is("My Neighbor Totoro")))
                .andExpect(jsonPath("$.items[0].type", is(AnimeType.TV.toString())))
                .andExpect(jsonPath("$.items[0].status", is(AnimeStatus.WATCHING.toString())))
                .andExpect(jsonPath("$.items[0].rating", is(5)))
                .andExpect(jsonPath("$.items[0].favorite", is(false)))
                .andExpect(jsonPath("$.items[0].complete", is(false)))
                .andExpect(jsonPath("$.items[0].studioId", is(((int) studio.getId()))));
    }

    @Test
    @Order(5)
    @DisplayName("Get Anime By ID - Success")
    @WithMockUser(username = "john.doe")
    void shouldGetAnimeById() throws Exception {
        animeRepository.save(anime);
        mockMvc.perform(get(API_URL + "/{id}", anime.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(anime.getTitle())))
                .andExpect(jsonPath("$.type", is(anime.getType().toString())))
                .andExpect(jsonPath("$.status", is(anime.getStatus().toString())))
                .andExpect(jsonPath("$.rating", is(anime.getRating())))
                .andExpect(jsonPath("$.favorite", is(anime.isFavorite())))
                .andExpect(jsonPath("$.complete", is(anime.isComplete())))
                .andExpect(jsonPath("$.studioId", is(((int) anime.getStudio().getId()))));
    }

    @Test
    @Order(6)
    @DisplayName("Get Anime By ID - Not Found")
    @WithMockUser(username = "john.doe")
    void shouldThrowExceptionWhenAnimeNotFoundById() throws Exception {
        mockMvc.perform(get(API_URL + "/{id}", 999)).andExpect(status().isNotFound());
    }

    @Test
    @Order(7)
    @DisplayName("Update Anime - Success")
    @WithMockUser(username = "john.doe")
    void shouldUpdateAnime() throws Exception {
        animeRepository.save(anime);
        AnimeDTO updatedAnime = AnimeDTO.builder()
                .title("My Neighbor Totoro")
                .type(AnimeType.MOVIE)
                .status(AnimeStatus.COMPLETED)
                .rating(8)
                .isFavorite(true)
                .isComplete(false)
                .studioId(studio.getId())
                .build();
        mockMvc.perform(put(API_URL + "/{id}", anime.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedAnime)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(updatedAnime.getTitle())))
                .andExpect(jsonPath("$.type", is(updatedAnime.getType().toString())))
                .andExpect(jsonPath("$.status", is(updatedAnime.getStatus().toString())))
                .andExpect(jsonPath("$.rating", is(updatedAnime.getRating())))
                .andExpect(jsonPath("$.favorite", is(updatedAnime.isFavorite())))
                .andExpect(jsonPath("$.complete", is(updatedAnime.isComplete())))
                .andExpect(jsonPath("$.studioId", is(updatedAnime.getStudioId().intValue())));
    }

    @Test
    @Order(8)
    @DisplayName("Update Anime - Not Found")
    @WithMockUser(username = "john.doe")
    void shouldThrowExceptionWhenUpdatingAnimeNotFound() throws Exception {
        AnimeDTO updatedAnime = AnimeDTO.builder()
                .title("My Neighbor Totoro")
                .type(AnimeType.MOVIE)
                .status(AnimeStatus.COMPLETED)
                .rating(8)
                .isFavorite(true)
                .isComplete(false)
                .studioId(studio.getId())
                .build();
        performPutRequest(updatedAnime, 999, 404);
    }

    @Test
    @Order(9)
    @DisplayName("Update Anime - Duplicate Title")
    @WithMockUser(username = "john.doe")
    void shouldThrowExceptionWhenUpdatingAnimeWithDuplicateTitle() throws Exception {
        animeRepository.save(anime);
        Anime newAnime = Anime.builder()
                .title("My Neighbor Totoro")
                .type(AnimeType.TV)
                .status(AnimeStatus.WATCHING)
                .rating(5)
                .isFavorite(false)
                .isComplete(false)
                .studio(studio)
                .user(user)
                .build();
        animeRepository.save(newAnime);
        AnimeDTO updatedAnime = AnimeDTO.builder()
                .title("My Neighbor Totoro")
                .type(AnimeType.MOVIE)
                .status(AnimeStatus.COMPLETED)
                .rating(8)
                .isFavorite(true)
                .isComplete(false)
                .studioId(studio.getId())
                .build();
        performPutRequest(updatedAnime, anime.getId(), 409);
    }

    @Test
    @Order(10)
    @DisplayName("Delete Anime - Success")
    @WithMockUser(username = "john.doe")
    void shouldDeleteAnime() throws Exception {
        animeRepository.save(anime);
        mockMvc.perform(delete(API_URL + "/{id}", anime.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @Order(11)
    @DisplayName("Delete Anime - Not Found")
    @WithMockUser(username = "john.doe")
    void shouldThrowExceptionWhenDeletingAnimeNotFound() throws Exception {
        performDeleteRequest();
    }

    @Test
    @Order(12)
    @DisplayName("Search Anime - Success")
    @WithMockUser(username = "john.doe")
    void shouldSearchAnime() throws Exception {
        animeRepository.save(anime);
        mockMvc.perform(get(API_URL + "/search")
                        .param("title", "Spirited Away")
                        .param("type", AnimeType.MOVIE.toString())
                        .param("status", AnimeStatus.COMPLETED.toString())
                        .param("rating", "10")
                        .param("isFavorite", "true")
                        .param("isComplete", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].title", is("Spirited Away")))
                .andExpect(jsonPath("$[0].type", is(AnimeType.MOVIE.toString())))
                .andExpect(jsonPath("$[0].status", is(AnimeStatus.COMPLETED.toString())))
                .andExpect(jsonPath("$[0].rating", is(10)))
                .andExpect(jsonPath("$[0].favorite", is(true)))
                .andExpect(jsonPath("$[0].complete", is(true)))
                .andExpect(jsonPath("$[0].studioId", is(((int) studio.getId()))));
    }

    @Test
    @Order(13)
    @DisplayName("Get Mean Rating - Success")
    @WithMockUser(username = "john.doe")
    void shouldGetMeanRating() throws Exception {
        Anime anime1 = Anime.builder()
                .title("Spirited Away")
                .type(AnimeType.MOVIE)
                .status(AnimeStatus.COMPLETED)
                .rating(10)
                .isFavorite(true)
                .isComplete(true)
                .studio(studio)
                .user(user)
                .build();
        animeRepository.save(anime1);
        Anime anime2 = Anime.builder()
                .title("My Neighbor Totoro")
                .type(AnimeType.TV)
                .status(AnimeStatus.WATCHING)
                .rating(8)
                .isFavorite(false)
                .isComplete(false)
                .studio(studio)
                .user(user)
                .build();
        animeRepository.save(anime2);
        Double meanRating = (anime1.getRating() + anime2.getRating()) / 2.0;
        mockMvc.perform(get(API_URL + "/mean-rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(meanRating)));
    }
}
