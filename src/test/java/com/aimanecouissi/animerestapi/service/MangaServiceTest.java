package com.aimanecouissi.animerestapi.service;

import com.aimanecouissi.animerestapi.entity.Manga;
import com.aimanecouissi.animerestapi.entity.Role;
import com.aimanecouissi.animerestapi.entity.User;
import com.aimanecouissi.animerestapi.enums.MangaStatus;
import com.aimanecouissi.animerestapi.exception.ResourceNotFoundException;
import com.aimanecouissi.animerestapi.exception.UniqueFieldException;
import com.aimanecouissi.animerestapi.payload.dto.MangaDTO;
import com.aimanecouissi.animerestapi.payload.response.MangaPaginatedResponse;
import com.aimanecouissi.animerestapi.repository.MangaRepository;
import com.aimanecouissi.animerestapi.repository.UserRepository;
import com.aimanecouissi.animerestapi.service.implementation.MangaServiceImplementation;
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
class MangaServiceTest {

    @Mock
    private MangaRepository mangaRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private MangaServiceImplementation mangaService;

    private User user;
    private Manga manga;
    private MangaDTO mangaDTO;

    @BeforeEach
    void setUp() {
        // Create a role for user
        Role role = Role.builder()
                .name("ROLE_USER")
                .build();

        // Create a user for manga
        user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .username("john.doe")
                .password("password123")
                .roles(Set.of(role))
                .build();

        // Manga BO
        manga = Manga.builder()
                .id(1L)
                .title("One Piece")
                .status(MangaStatus.READING)
                .rating(10)
                .isFavorite(true)
                .user(user)
                .build();

        // Manga DTO
        mangaDTO = MangaDTO.builder()
                .title("One Piece")
                .status(MangaStatus.READING)
                .rating(10)
                .isFavorite(true)
                .build();

        SecurityContextHolder.getContext().setAuthentication(authentication);
        given(authentication.getName()).willReturn(user.getUsername());
        given(userRepository.findByUsername(user.getUsername())).willReturn(Optional.of(user));
    }

    @Test
    @Order(1)
    @DisplayName("Create Manga - Success")
    void shouldCreateManga() {
        given(mangaRepository.existsByTitleAndUserId(mangaDTO.getTitle(), user.getId())).willReturn(false);
        given(mangaRepository.save(any(Manga.class))).willReturn(manga);
        given(modelMapper.map(mangaDTO, Manga.class)).willReturn(manga);
        given(modelMapper.map(manga, MangaDTO.class)).willReturn(mangaDTO);
        MangaDTO savedMangaDTO = mangaService.createManga(mangaDTO);
        assertThat(savedMangaDTO).isNotNull();
        assertThat(savedMangaDTO.getTitle()).isEqualTo(mangaDTO.getTitle());
        verify(mangaRepository).save(any(Manga.class));
    }

    @Test
    @Order(2)
    @DisplayName("Create Manga - Duplicate Title")
    void shouldThrowExceptionWhenCreatingMangaWithDuplicateTitle() {
        given(mangaRepository.existsByTitleAndUserId(mangaDTO.getTitle(), user.getId())).willReturn(true);
        UniqueFieldException exception = assertThrows(UniqueFieldException.class, () -> mangaService.createManga(mangaDTO));
        assertThat(exception.getMessage()).contains("Title", mangaDTO.getTitle());
        verify(mangaRepository, never()).save(any(Manga.class));
    }

    @Test
    @Order(3)
    @DisplayName("Get All Manga - Success")
    void shouldGetAllManga() {
        PageRequest pageRequest = PageRequest.of(
                0,
                10, Sort.Direction.ASC,
                "title"
        );
        Page<Manga> mangaPage = new PageImpl<>(List.of(manga), pageRequest, 1);
        given(mangaRepository.findAllByUserId(user.getId(), pageRequest)).willReturn(mangaPage);
        given(modelMapper.map(manga, MangaDTO.class)).willReturn(mangaDTO);
        MangaPaginatedResponse response = mangaService.getAllManga(
                0,
                10,
                "title",
                "asc"
        );
        assertThat(response).isNotNull();
        assertThat(response.getItems()).isNotEmpty();
        assertThat(response.getItems()).contains(mangaDTO);
    }

    @Test
    @Order(4)
    @DisplayName("Get Manga By ID - Success")
    void shouldGetMangaById() {
        given(mangaRepository.findById(manga.getId())).willReturn(Optional.of(manga));
        given(modelMapper.map(manga, MangaDTO.class)).willReturn(mangaDTO);
        MangaDTO foundMangaDTO = mangaService.getMangaById(manga.getId());
        assertThat(foundMangaDTO).isNotNull();
        assertThat(foundMangaDTO.getTitle()).isEqualTo(manga.getTitle());
    }

    @Test
    @Order(5)
    @DisplayName("Get Manga By ID - Not Found")
    void shouldThrowExceptionWhenMangaNotFoundById() {
        given(mangaRepository.findById(manga.getId())).willReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> mangaService.getMangaById(manga.getId()));
        assertThat(exception.getMessage()).contains("Manga", "ID", String.valueOf(manga.getId()));
    }

    @Test
    @Order(6)
    @DisplayName("Update Manga - Success")
    void shouldUpdateManga() {
        Manga updatedManga = Manga.builder()
                .id(1L)
                .title("Boruto: Two Blue Vortex")
                .status(MangaStatus.READING)
                .rating(8)
                .isFavorite(true)
                .user(user)
                .build();

        MangaDTO updatedMangaDTO = MangaDTO.builder()
                .title("Boruto: Two Blue Vortex")
                .status(MangaStatus.READING)
                .rating(8)
                .isFavorite(true)
                .build();

        given(mangaRepository.findById(manga.getId())).willReturn(Optional.of(manga));
        given(mangaRepository.existsByTitleAndUserId(updatedMangaDTO.getTitle(), user.getId())).willReturn(false);
        given(mangaRepository.save(any(Manga.class))).willReturn(updatedManga);
        given(modelMapper.map(updatedManga, MangaDTO.class)).willReturn(updatedMangaDTO);
        MangaDTO savedUpdatedMangaDTO = mangaService.updateManga(manga.getId(), updatedMangaDTO);
        assertThat(savedUpdatedMangaDTO).isNotNull();
        assertThat(savedUpdatedMangaDTO.getTitle()).isEqualTo(updatedManga.getTitle());
        verify(mangaRepository).save(any(Manga.class));
    }

    @Test
    @Order(7)
    @DisplayName("Update Manga - Duplicate Title")
    void shouldThrowExceptionWhenUpdatingMangaWithDuplicateTitle() {
        MangaDTO updatedMangaDTO = MangaDTO.builder()
                .title("Boruto: Two Blue Vortex")
                .status(MangaStatus.READING)
                .rating(8)
                .isFavorite(true)
                .build();
        given(mangaRepository.findById(manga.getId())).willReturn(Optional.of(manga));
        given(mangaRepository.existsByTitleAndUserId(updatedMangaDTO.getTitle(), user.getId())).willReturn(true);
        UniqueFieldException exception = assertThrows(UniqueFieldException.class, () -> mangaService.updateManga(manga.getId(), updatedMangaDTO));
        assertThat(exception.getMessage()).contains("Title", updatedMangaDTO.getTitle());
        verify(mangaRepository, never()).save(any(Manga.class));
    }

    @Test
    @Order(8)
    @DisplayName("Delete Manga - Success")
    void shouldDeleteManga() {
        given(mangaRepository.findById(manga.getId())).willReturn(Optional.of(manga));
        mangaService.deleteManga(manga.getId());
        verify(mangaRepository).delete(manga);
    }

    @Test
    @Order(9)
    @DisplayName("Delete Manga - Not Found")
    void shouldThrowExceptionWhenDeletingNonExistentManga() {
        given(mangaRepository.findById(manga.getId())).willReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> mangaService.deleteManga(manga.getId()));
        assertThat(exception.getMessage()).contains("Manga", "ID", String.valueOf(manga.getId()));
    }

    @Test
    @Order(10)
    @DisplayName("Search Manga - Success")
    void shouldSearchManga() {
        given(mangaRepository.findAllByUserIdAndFilters(
                user.getId(),
                "One Piece",
                MangaStatus.READING,
                10,
                true
        )).willReturn(List.of(manga));
        given(modelMapper.map(manga, MangaDTO.class)).willReturn(mangaDTO);
        List<MangaDTO> result = mangaService.searchManga(
                "One Piece",
                MangaStatus.READING,
                10,
                true
        );
        assertThat(result).isNotEmpty();
        assertThat(result).contains(mangaDTO);
    }

    @Test
    @Order(11)
    @DisplayName("Get Mean Rating - Success")
    void shouldGetMeanRating() {
        given(mangaRepository.findAverageRatingByUserId(user.getId())).willReturn(Optional.of(10.0));
        Double meanRating = mangaService.getMeanRating();
        assertThat(meanRating).isEqualTo(10.0);
    }

    @Test
    @Order(12)
    @DisplayName("Get Mean Rating - No Manga")
    void shouldGetMeanRatingWithNoManga() {
        given(mangaRepository.findAverageRatingByUserId(user.getId())).willReturn(Optional.empty());
        Double meanRating = mangaService.getMeanRating();
        assertThat(meanRating).isEqualTo(0.0);
    }
}
