package com.aimanecouissi.animerestapi.service.implementation;

import com.aimanecouissi.animerestapi.entity.Manga;
import com.aimanecouissi.animerestapi.entity.User;
import com.aimanecouissi.animerestapi.enums.MangaStatus;
import com.aimanecouissi.animerestapi.exception.ResourceNotFoundException;
import com.aimanecouissi.animerestapi.exception.UnauthorizedOperationException;
import com.aimanecouissi.animerestapi.exception.UniqueFieldException;
import com.aimanecouissi.animerestapi.payload.dto.MangaDTO;
import com.aimanecouissi.animerestapi.payload.response.MangaPaginatedResponse;
import com.aimanecouissi.animerestapi.repository.MangaRepository;
import com.aimanecouissi.animerestapi.repository.UserRepository;
import com.aimanecouissi.animerestapi.service.MangaService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MangaServiceImplementation implements MangaService {
    private final MangaRepository mangaRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public MangaServiceImplementation(MangaRepository mangaRepository, UserRepository userRepository, ModelMapper modelMapper) {
        this.mangaRepository = mangaRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public MangaDTO createManga(MangaDTO mangaDTO) {
        User currentUser = getCurrentUser();
        if (mangaRepository.existsByTitleAndUserId(mangaDTO.getTitle(), currentUser.getId())) {
            throw new UniqueFieldException("Title", mangaDTO.getTitle());
        }
        Manga manga = toManga(mangaDTO);
        manga.setUser(currentUser);
        return toMangaDTO(mangaRepository.save(manga));
    }

    @Override
    @Transactional(readOnly = true)
    public MangaPaginatedResponse getAllManga(int pageNo, int pageSize, String sortBy, String sortDir) {
        User currentUser = getCurrentUser();
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.Direction.fromString(sortDir), sortBy);
        Page<Manga> mangaPage = mangaRepository.findAllByUserId(currentUser.getId(), pageRequest);
        return toMangaPaginatedResponse(mangaPage);
    }

    @Override
    @Transactional(readOnly = true)
    public MangaDTO getMangaById(long id) {
        Manga manga = getMangaByIdAndUser(id, getCurrentUser().getId());
        return toMangaDTO(manga);
    }

    @Override
    @Transactional
    public MangaDTO updateManga(long id, MangaDTO mangaDTO) {
        Manga manga = getMangaByIdAndUser(id, getCurrentUser().getId());
        if (!manga.getTitle().equals(mangaDTO.getTitle()) && mangaRepository.existsByTitleAndUserId(mangaDTO.getTitle(), manga.getUser().getId())) {
            throw new UniqueFieldException("Title", mangaDTO.getTitle());
        }
        updateMangaFields(manga, mangaDTO);
        return toMangaDTO(mangaRepository.save(manga));
    }

    @Override
    @Transactional
    public void deleteManga(long id) {
        Manga manga = getMangaByIdAndUser(id, getCurrentUser().getId());
        mangaRepository.delete(manga);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MangaDTO> searchManga(String title, MangaStatus status, Integer rating, Boolean isFavorite) {
        return mangaRepository.findAllByUserIdAndFilters(
                        getCurrentUser().getId(),
                        title,
                        status,
                        rating,
                        isFavorite
                ).stream()
                .map(this::toMangaDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Double getMeanRating() {
        User currentUser = getCurrentUser();
        return mangaRepository.findAverageRatingByUserId(currentUser.getId())
                .orElse(0.0);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private Manga getMangaByIdAndUser(long mangaId, long userId) {
        Manga manga = mangaRepository.findById(mangaId)
                .orElseThrow(() -> new ResourceNotFoundException("Manga", "ID", String.valueOf(mangaId)));
        if (manga.getUser().getId() != userId) {
            throw new UnauthorizedOperationException("You do not have permission to access this manga.");
        }
        return manga;
    }

    private MangaDTO toMangaDTO(Manga manga) {
        return modelMapper.map(manga, MangaDTO.class);
    }

    private Manga toManga(MangaDTO mangaDTO) {
        return modelMapper.map(mangaDTO, Manga.class);
    }

    private void updateMangaFields(Manga manga, MangaDTO mangaDTO) {
        manga.setTitle(mangaDTO.getTitle());
        manga.setStatus(mangaDTO.getStatus());
        manga.setRating(mangaDTO.getRating());
        manga.setFavorite(mangaDTO.isFavorite());
    }

    private MangaPaginatedResponse toMangaPaginatedResponse(Page<Manga> mangaPage) {
        List<MangaDTO> mangaDTOs = mangaPage.getContent()
                .stream()
                .map(this::toMangaDTO)
                .collect(Collectors.toList());
        return MangaPaginatedResponse.builder()
                .items(mangaDTOs)
                .totalPages(mangaPage.getTotalPages())
                .totalElements(mangaPage.getTotalElements())
                .pageSize(mangaPage.getSize())
                .pageNumber(mangaPage.getNumber())
                .isLast(mangaPage.isLast())
                .build();
    }
}
