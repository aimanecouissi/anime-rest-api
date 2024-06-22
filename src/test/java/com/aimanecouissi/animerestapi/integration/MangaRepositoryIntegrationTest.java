package com.aimanecouissi.animerestapi.integration;

import com.aimanecouissi.animerestapi.entity.Manga;
import com.aimanecouissi.animerestapi.entity.Role;
import com.aimanecouissi.animerestapi.entity.User;
import com.aimanecouissi.animerestapi.enums.MangaStatus;
import com.aimanecouissi.animerestapi.repository.MangaRepository;
import com.aimanecouissi.animerestapi.repository.RoleRepository;
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
class MangaRepositoryIntegrationTest {

    private static User user;

    @Autowired
    private MangaRepository mangaRepository;

    private Manga manga;

    @BeforeAll
    static void init(
            @Autowired UserRepository userRepository,
            @Autowired RoleRepository roleRepository
    ) {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create a role for user
        Role role = Role.builder().name("ROLE_USER").build();
        roleRepository.save(role);

        // Create a user for manga
        user = User.builder()
                .firstName("Alice")
                .lastName("Smith")
                .username("alice.smith")
                .password("password123")
                .roles(Set.of(role))
                .build();
        userRepository.save(user);
    }

    @BeforeEach
    void setUp() {
        mangaRepository.deleteAll();
        manga = Manga.builder()
                .title("Naruto")
                .status(MangaStatus.READING)
                .rating(9)
                .isFavorite(true)
                .user(user)
                .build();
        mangaRepository.save(manga);
    }

    @Test
    @Order(1)
    @DisplayName("Save Manga")
    void shouldSaveManga() {
        Manga newManga = Manga.builder()
                .title("One Piece")
                .status(MangaStatus.COMPLETED)
                .rating(10)
                .isFavorite(true)
                .user(user)
                .build();
        Manga savedManga = mangaRepository.save(newManga);
        assertThat(savedManga).isNotNull();
        assertThat(savedManga.getId()).isGreaterThan(0);
        assertThat(savedManga).usingRecursiveComparison()
                .ignoringFields("id", "createdAt", "updatedAt")
                .isEqualTo(newManga);
    }

    @Test
    @Order(2)
    @DisplayName("Find All Manga")
    void shouldFindAllMangas() {
        Manga manga1 = Manga.builder()
                .title("Bleach")
                .status(MangaStatus.READING)
                .rating(8)
                .isFavorite(true)
                .user(user)
                .build();
        mangaRepository.save(manga1);
        Manga manga2 = Manga.builder()
                .title("Attack on Titan")
                .status(MangaStatus.COMPLETED)
                .rating(9)
                .isFavorite(false)
                .user(user)
                .build();
        mangaRepository.save(manga2);
        List<Manga> mangaList = mangaRepository.findAll();
        assertThat(mangaList.size()).isEqualTo(3);
        assertThat(mangaList).contains(manga, manga1, manga2);
    }

    @Test
    @Order(5)
    @DisplayName("Find Manga By ID")
    void shouldFindMangaById() {
        Optional<Manga> foundManga = mangaRepository.findById(manga.getId());
        assertThat(foundManga).isPresent();
        assertThat(foundManga.get()).usingRecursiveComparison()
                .ignoringFields("id", "createdAt", "updatedAt")
                .isEqualTo(manga);
    }

    @Test
    @Order(7)
    @DisplayName("Update Manga")
    void shouldUpdateManga() {
        manga.setTitle("Naruto: Shippuden");
        Manga updatedManga = mangaRepository.save(manga);
        assertThat(updatedManga.getTitle()).isEqualTo("Naruto: Shippuden");
    }

    @Test
    @Order(8)
    @DisplayName("Delete Manga")
    void shouldDeleteManga() {
        mangaRepository.delete(manga);
        List<Manga> mangaList = mangaRepository.findAll();
        assertThat(mangaList).isEmpty();
    }

    @Test
    @Order(6)
    @DisplayName("Find Manga By Title")
    void shouldFindMangaByTitle() {
        Optional<Manga> foundManga = mangaRepository.findByTitle(manga.getTitle());
        assertThat(foundManga).isPresent();
        assertThat(foundManga.get()).usingRecursiveComparison()
                .ignoringFields("id", "createdAt", "updatedAt")
                .isEqualTo(manga);
    }

    @Test
    @Order(9)
    @DisplayName("Check Manga Existence By Title and User ID")
    void shouldCheckMangaExistenceByTitleAndUserId() {
        boolean exists = mangaRepository.existsByTitleAndUserId(manga.getTitle(), user.getId());
        assertThat(exists).isTrue();
    }

    @Test
    @Order(3)
    @DisplayName("Find All Manga By User ID")
    void shouldFindAllByUserId() {
        Page<Manga> mangaList = mangaRepository.findAllByUserId(user.getId(), PageRequest.of(0, 10));
        assertThat(mangaList).isNotEmpty();
        assertThat(mangaList.getContent()).contains(manga);
    }

    @Test
    @Order(10)
    @DisplayName("Find Average Rating By User ID")
    void shouldFindAverageRatingByUserId() {
        Optional<Double> averageRating = mangaRepository.findAverageRatingByUserId(user.getId());
        assertThat(averageRating).isPresent();
        assertThat(averageRating.get()).isEqualTo(9.0);
    }

    @Test
    @Order(4)
    @DisplayName("Find All Manga By User ID and Filters")
    void shouldFindAllByUserIdAndFilters() {
        List<Manga> mangaList = mangaRepository.findAllByUserIdAndFilters(
                user.getId(),
                "Naruto",
                MangaStatus.READING,
                9,
                true
        );
        assertThat(mangaList).isNotEmpty();
        assertThat(mangaList).contains(manga);
    }
}
