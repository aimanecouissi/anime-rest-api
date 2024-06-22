package com.aimanecouissi.animerestapi.repository;

import com.aimanecouissi.animerestapi.entity.Anime;
import com.aimanecouissi.animerestapi.entity.Role;
import com.aimanecouissi.animerestapi.entity.Studio;
import com.aimanecouissi.animerestapi.entity.User;
import com.aimanecouissi.animerestapi.enums.AnimeStatus;
import com.aimanecouissi.animerestapi.enums.AnimeType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestMethodOrder(OrderAnnotation.class)
class AnimeRepositoryTest {

    @Autowired
    private AnimeRepository animeRepository;

    @Autowired
    private StudioRepository studioRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User user;
    private Studio studio;
    private Anime anime;

    @BeforeEach
    void setUp() {
        // Create a role for user
        Role role = Role.builder()
                .name("ROLE_USER")
                .build();
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
        studio = Studio.builder()
                .name("MAPPA")
                .build();
        studioRepository.save(studio);

        // Create an anime
        anime = Anime.builder()
                .title("Attack on Titan")
                .type(AnimeType.TV)
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
                .title("Chainsaw Man")
                .type(AnimeType.TV)
                .status(AnimeStatus.COMPLETED)
                .rating(9)
                .isFavorite(true)
                .isComplete(false)
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
                .title("Jujutsu Kaisen")
                .type(AnimeType.TV)
                .status(AnimeStatus.COMPLETED)
                .rating(9)
                .isFavorite(true)
                .isComplete(false)
                .studio(studio)
                .user(user)
                .build();
        animeRepository.save(anime1);
        Anime anime2 = Anime.builder()
                .title("Vinland Saga")
                .type(AnimeType.TV)
                .status(AnimeStatus.COMPLETED)
                .rating(9)
                .isFavorite(true)
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
        anime.setTitle("Shingeki no Kyojin");
        Anime updatedAnime = animeRepository.save(anime);
        assertThat(updatedAnime.getTitle()).isEqualTo("Shingeki no Kyojin");
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
        Page<Anime> animeList = animeRepository.findAllByUserId(
                user.getId(),
                PageRequest.of(0, 10)
        );
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
                "Attack",
                AnimeType.TV,
                AnimeStatus.COMPLETED,
                10,
                true,
                true
        );
        assertThat(animeList).isNotEmpty();
        assertThat(animeList).contains(anime);
    }
}
