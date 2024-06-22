package com.aimanecouissi.animerestapi.service;

import com.aimanecouissi.animerestapi.entity.Anime;
import com.aimanecouissi.animerestapi.entity.Role;
import com.aimanecouissi.animerestapi.entity.Studio;
import com.aimanecouissi.animerestapi.entity.User;
import com.aimanecouissi.animerestapi.enums.AnimeStatus;
import com.aimanecouissi.animerestapi.enums.AnimeType;
import com.aimanecouissi.animerestapi.exception.ResourceNotFoundException;
import com.aimanecouissi.animerestapi.exception.UniqueFieldException;
import com.aimanecouissi.animerestapi.payload.dto.AnimeDTO;
import com.aimanecouissi.animerestapi.payload.response.AnimePaginatedResponse;
import com.aimanecouissi.animerestapi.repository.AnimeRepository;
import com.aimanecouissi.animerestapi.repository.RoleRepository;
import com.aimanecouissi.animerestapi.repository.StudioRepository;
import com.aimanecouissi.animerestapi.repository.UserRepository;
import com.aimanecouissi.animerestapi.service.implementation.AnimeServiceImplementation;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(OrderAnnotation.class)
class AnimeServiceTest {

    @Mock
    private AnimeRepository animeRepository;

    @Mock
    private StudioRepository studioRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AnimeServiceImplementation animeService;

    private Role role;
    private User user;
    private Studio studio;
    private Anime anime;
    private AnimeDTO animeDTO;

    @BeforeEach
    void setUp() {
        // Role for user
        Role role = Role.builder()
                .id(1L)
                .name("ROLE_USER")
                .build();
        // User for anime
        user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .username("john.doe")
                .password("password123")
                .roles(Set.of(role))
                .build();

        // Studio for anime
        studio = Studio.builder()
                .id(1L)
                .name("MAPPA")
                .build();

        // Anime BO
        anime = Anime.builder()
                .id(1L)
                .title("Attack on Titan")
                .type(AnimeType.TV)
                .status(AnimeStatus.COMPLETED)
                .rating(10)
                .isFavorite(true)
                .isComplete(true)
                .studio(studio)
                .user(user)
                .build();


        // Anime DTO
        animeDTO = AnimeDTO.builder()
                .title("Attack on Titan")
                .type(AnimeType.TV)
                .status(AnimeStatus.COMPLETED)
                .rating(10)
                .isFavorite(true)
                .isComplete(true)
                .studioId(studio.getId())
                .build();

        SecurityContextHolder.getContext().setAuthentication(authentication);
        given(authentication.getName()).willReturn(user.getUsername());
        given(userRepository.findByUsername(user.getUsername())).willReturn(Optional.of(user));
    }

    @Test
    @Order(1)
    @DisplayName("Create Anime - Success")
    void shouldCreateAnime() {
        given(animeRepository.existsByTitleAndUserId(animeDTO.getTitle(), user.getId())).willReturn(false);
        given(studioRepository.findById(studio.getId())).willReturn(Optional.of(studio));
        given(animeRepository.save(any(Anime.class))).willReturn(anime);
        given(modelMapper.map(animeDTO, Anime.class)).willReturn(anime);
        given(modelMapper.map(anime, AnimeDTO.class)).willReturn(animeDTO);
        AnimeDTO savedAnimeDTO = animeService.createAnime(animeDTO);
        assertThat(savedAnimeDTO).isNotNull();
        assertThat(savedAnimeDTO.getTitle()).isEqualTo(animeDTO.getTitle());
        verify(animeRepository).save(any(Anime.class));
    }

    @Test
    @Order(2)
    @DisplayName("Create Anime - Duplicate Title")
    void shouldThrowExceptionWhenCreatingAnimeWithDuplicateTitle() {
        given(animeRepository.existsByTitleAndUserId(animeDTO.getTitle(), user.getId())).willReturn(true);
        UniqueFieldException exception = assertThrows(UniqueFieldException.class, () -> animeService.createAnime(animeDTO));
        assertThat(exception.getMessage()).contains("Title", animeDTO.getTitle());
        verify(animeRepository, never()).save(any(Anime.class));
    }

    @Test
    @Order(3)
    @DisplayName("Get All Anime - Success")
    void shouldGetAllAnime() {
        PageRequest pageRequest = PageRequest.of(
                0,
                10,
                Sort.Direction.ASC,
                "title"
        );
        Page<Anime> animePage = new PageImpl<>(List.of(anime), pageRequest, 1);
        given(animeRepository.findAllByUserId(user.getId(), pageRequest)).willReturn(animePage);
        given(modelMapper.map(anime, AnimeDTO.class)).willReturn(animeDTO);
        AnimePaginatedResponse response = animeService.getAllAnime(
                0,
                10,
                "title",
                "asc"
        );
        assertThat(response).isNotNull();
        assertThat(response.getItems()).isNotEmpty();
        assertThat(response.getItems()).contains(animeDTO);
    }

    @Test
    @Order(4)
    @DisplayName("Get Anime By ID - Success")
    void shouldGetAnimeById() {
        given(animeRepository.findById(anime.getId())).willReturn(Optional.of(anime));
        given(modelMapper.map(anime, AnimeDTO.class)).willReturn(animeDTO);
        AnimeDTO foundAnimeDTO = animeService.getAnimeById(anime.getId());
        assertThat(foundAnimeDTO).isNotNull();
        assertThat(foundAnimeDTO.getTitle()).isEqualTo(anime.getTitle());
    }

    @Test
    @Order(5)
    @DisplayName("Get Anime By ID - Not Found")
    void shouldThrowExceptionWhenAnimeNotFoundById() {
        given(animeRepository.findById(anime.getId())).willReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> animeService.getAnimeById(anime.getId()));
        assertThat(exception.getMessage()).contains("Anime", "ID", String.valueOf(anime.getId()));
    }

    @Test
    @Order(7)
    @DisplayName("Update Anime - Success")
    void shouldUpdateAnime() {
        Anime updatedAnime = Anime.builder()
                .id(1L)
                .title("Shingeki no Kyojin")
                .type(AnimeType.TV)
                .status(AnimeStatus.COMPLETED)
                .rating(10)
                .isFavorite(true)
                .isComplete(true)
                .studio(studio)
                .user(user)
                .build();
        AnimeDTO updatedAnimeDTO = AnimeDTO.builder()
                .title("Shingeki no Kyojin")
                .type(AnimeType.TV)
                .status(AnimeStatus.COMPLETED)
                .rating(10)
                .isFavorite(true)
                .isComplete(true)
                .studioId(studio.getId())
                .build();
        given(animeRepository.findById(anime.getId())).willReturn(Optional.of(anime));
        given(animeRepository.existsByTitleAndUserId(updatedAnimeDTO.getTitle(), user.getId())).willReturn(false);
        given(studioRepository.findById(studio.getId())).willReturn(Optional.of(studio));
        given(animeRepository.save(any(Anime.class))).willReturn(updatedAnime);
        given(modelMapper.map(updatedAnime, AnimeDTO.class)).willReturn(updatedAnimeDTO);
        AnimeDTO savedUpdatedAnimeDTO = animeService.updateAnime(anime.getId(), updatedAnimeDTO);
        assertThat(savedUpdatedAnimeDTO).isNotNull();
        assertThat(savedUpdatedAnimeDTO.getTitle()).isEqualTo(updatedAnime.getTitle());
        verify(animeRepository).save(any(Anime.class));
    }

    @Test
    @Order(8)
    @DisplayName("Update Anime - Duplicate Title")
    void shouldThrowExceptionWhenUpdatingAnimeWithDuplicateTitle() {
        AnimeDTO updatedAnimeDTO = AnimeDTO.builder()
                .title("Shingeki no Kyojin")
                .type(AnimeType.TV)
                .status(AnimeStatus.COMPLETED)
                .rating(10)
                .isFavorite(true)
                .isComplete(true)
                .studioId(studio.getId())
                .build();
        given(animeRepository.findById(anime.getId())).willReturn(Optional.of(anime));
        given(animeRepository.existsByTitleAndUserId(updatedAnimeDTO.getTitle(), user.getId())).willReturn(true);
        UniqueFieldException exception = assertThrows(UniqueFieldException.class, () -> animeService.updateAnime(anime.getId(), updatedAnimeDTO));
        assertThat(exception.getMessage()).contains("Title", updatedAnimeDTO.getTitle());
        verify(animeRepository, never()).save(any(Anime.class));
    }

    @Test
    @Order(10)
    @DisplayName("Delete Anime - Success")
    void shouldDeleteAnime() {
        given(animeRepository.findById(anime.getId())).willReturn(Optional.of(anime));
        animeService.deleteAnime(anime.getId());
        verify(animeRepository).delete(anime);
    }

    @Test
    @Order(9)
    @DisplayName("Delete Anime - Not Found")
    void shouldThrowExceptionWhenDeletingNonExistentAnime() {
        given(animeRepository.findById(anime.getId())).willReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> animeService.deleteAnime(anime.getId()));
        assertThat(exception.getMessage()).contains("Anime", "ID", String.valueOf(anime.getId()));
    }

    @Test
    @Order(6)
    @DisplayName("Get Anime By Studio ID - Success")
    void shouldGetAnimeByStudioId() {
        given(studioRepository.findById(studio.getId())).willReturn(Optional.of(studio));
        given(animeRepository.findByStudioIdAndUserId(studio.getId(), user.getId())).willReturn(List.of(anime));
        given(modelMapper.map(anime, AnimeDTO.class)).willReturn(animeDTO);
        List<AnimeDTO> result = animeService.getAnimeByStudioId(studio.getId());
        assertThat(result).isNotEmpty();
        assertThat(result).contains(animeDTO);
    }

    @Test
    @Order(11)
    @DisplayName("Search Anime - Success")
    void shouldSearchAnime() {
        given(animeRepository.findAllByUserIdAndFilters(
                user.getId(),
                "Spirited Away",
                AnimeType.MOVIE,
                AnimeStatus.COMPLETED,
                10, true,
                true
        )).willReturn(List.of(anime));
        given(modelMapper.map(anime, AnimeDTO.class)).willReturn(animeDTO);
        List<AnimeDTO> result = animeService.searchAnime(
                "Spirited Away",
                AnimeType.MOVIE,
                AnimeStatus.COMPLETED,
                10,
                true,
                true
        );
        assertThat(result).isNotEmpty();
        assertThat(result).contains(animeDTO);
    }

    @Test
    @Order(12)
    @DisplayName("Get Mean Rating - Success")
    void shouldGetMeanRating() {
        given(animeRepository.findAverageRatingByUserId(user.getId())).willReturn(Optional.of(10.0));
        Double meanRating = animeService.getMeanRating();
        assertThat(meanRating).isEqualTo(10.0);
    }

    @Test
    @Order(13)
    @DisplayName("Get Mean Rating - No Anime")
    void shouldGetMeanRatingWithNoAnime() {
        given(animeRepository.findAverageRatingByUserId(user.getId())).willReturn(Optional.empty());
        Double meanRating = animeService.getMeanRating();
        assertThat(meanRating).isEqualTo(0.0);
    }
}
