package com.aimanecouissi.animerestapi.integration;

import com.aimanecouissi.animerestapi.entity.Anime;
import com.aimanecouissi.animerestapi.entity.Role;
import com.aimanecouissi.animerestapi.entity.Studio;
import com.aimanecouissi.animerestapi.entity.User;
import com.aimanecouissi.animerestapi.enums.AnimeStatus;
import com.aimanecouissi.animerestapi.enums.AnimeType;
import com.aimanecouissi.animerestapi.repository.AnimeRepository;
import com.aimanecouissi.animerestapi.repository.RoleRepository;
import com.aimanecouissi.animerestapi.repository.StudioRepository;
import com.aimanecouissi.animerestapi.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(OrderAnnotation.class)
class AnimeRepositoryIntegrationTest {

    private static User user;
    private static Studio studio;

    @Autowired
    private AnimeRepository animeRepository;

    private Anime anime;

    @BeforeAll
    static void init(
            @Autowired UserRepository userRepository,
            @Autowired StudioRepository studioRepository,
            @Autowired RoleRepository roleRepository
    ) {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        studioRepository.deleteAll();

        // Create a role for user
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
        animeRepository.save(anime);
    }

    @Test
    @Order(1)
    @DisplayName("Save Anime")
    void shouldSaveAnime() {
        Anime newAnime = Anime.builder()
                .title("My Neighbor Totoro")
                .type(AnimeType.MOVIE)
                .status(AnimeStatus.COMPLETED)
                .rating(9)
                .isFavorite(true)
                .isComplete(true)
                .studio(studio)
                .user(user)
                .build();
        Anime savedAnime = animeRepository.save(newAnime);
        assertThat(savedAnime).isNotNull();
        assertThat(savedAnime.getId()).isGreaterThan(0);
        assertThat(savedAnime).usingRecursiveComparison()
                .ignoringFields("id", "createdAt", "updatedAt")
                .isEqualTo(newAnime);
    }

    @Test
    @Order(2)
    @DisplayName("Find All Anime")
    void shouldFindAllAnime() {
        Anime anime1 = Anime.builder()
                .title("Princess Mononoke")
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
                .title("Castle in the Sky")
                .type(AnimeType.MOVIE)
                .status(AnimeStatus.COMPLETED)
                .rating(8)
                .isFavorite(false)
                .isComplete(true)
                .studio(studio)
                .user(user)
                .build();
        animeRepository.save(anime2);
        List<Anime> animeList = animeRepository.findAll();
        assertThat(animeList.size()).isEqualTo(3);
        assertThat(animeList).contains(anime, anime1, anime2);
    }

    @Test
    @Order(6)
    @DisplayName("Find Anime By ID")
    void shouldFindAnimeById() {
        Optional<Anime> foundAnime = animeRepository.findById(anime.getId());
        assertThat(foundAnime).isPresent();
        assertThat(foundAnime.get()).usingRecursiveComparison()
                .ignoringFields("id", "createdAt", "updatedAt")
                .isEqualTo(anime);
    }

    @Test
    @Order(8)
    @DisplayName("Update Anime")
    void shouldUpdateAnime() {
        anime.setTitle("The Boy and the Heron");
        Anime updatedAnime = animeRepository.save(anime);
        assertThat(updatedAnime.getTitle()).isEqualTo("The Boy and the Heron");
    }

    @Test
    @Order(9)
    @DisplayName("Delete Anime")
    void shouldDeleteAnime() {
        animeRepository.delete(anime);
        List<Anime> animeList = animeRepository.findAll();
        assertThat(animeList).isEmpty();
    }

    @Test
    @Order(7)
    @DisplayName("Find Anime By Title")
    void shouldFindAnimeByTitle() {
        Optional<Anime> foundAnime = animeRepository.findByTitle(anime.getTitle());
        assertThat(foundAnime).isPresent();
        assertThat(foundAnime.get()).usingRecursiveComparison()
                .ignoringFields("id", "createdAt", "updatedAt")
                .isEqualTo(anime);
    }

    @Test
    @Order(10)
    @DisplayName("Check Anime Existence By Title and User ID")
    void shouldCheckAnimeExistenceByTitleAndUserId() {
        boolean exists = animeRepository.existsByTitleAndUserId(anime.getTitle(), user.getId());
        assertThat(exists).isTrue();
    }

    @Test
    @Order(3)
    @DisplayName("Find All Anime By Studio ID and User ID")
    void shouldFindAllByStudioIdAndUserId() {
        List<Anime> animeList = animeRepository.findByStudioIdAndUserId(studio.getId(), user.getId());
        assertThat(animeList).isNotEmpty();
        assertThat(animeList).contains(anime);
    }

    @Test
    @Order(4)
    @DisplayName("Find All Anime By User ID")
    void shouldFindAllByUserId() {
        Page<Anime> animeList = animeRepository.findAllByUserId(user.getId(), PageRequest.of(0, 10));
        assertThat(animeList).isNotEmpty();
        assertThat(animeList.getContent()).contains(anime);
    }

    @Test
    @Order(11)
    @DisplayName("Find Average Rating By User ID")
    void shouldFindAverageRatingByUserId() {
        Optional<Double> averageRating = animeRepository.findAverageRatingByUserId(user.getId());
        assertThat(averageRating).isPresent();
        assertThat(averageRating.get()).isEqualTo(10.0);
    }

    @Test
    @Order(5)
    @DisplayName("Find All Anime By User ID and Filters")
    void shouldFindAllByUserIdAndFilters() {
        List<Anime> animeList = animeRepository.findAllByUserIdAndFilters(
                user.getId(),
                "Spirited",
                AnimeType.MOVIE,
                AnimeStatus.COMPLETED,
                10,
                true,
                true
        );
        assertThat(animeList).isNotEmpty();
        assertThat(animeList).contains(anime);
    }
}
