package com.aimanecouissi.animerestapi.service.implementation;

import com.aimanecouissi.animerestapi.entity.Anime;
import com.aimanecouissi.animerestapi.entity.Studio;
import com.aimanecouissi.animerestapi.entity.User;
import com.aimanecouissi.animerestapi.enums.AnimeStatus;
import com.aimanecouissi.animerestapi.enums.AnimeType;
import com.aimanecouissi.animerestapi.exception.ResourceNotFoundException;
import com.aimanecouissi.animerestapi.exception.UnauthorizedOperationException;
import com.aimanecouissi.animerestapi.exception.UniqueFieldException;
import com.aimanecouissi.animerestapi.payload.dto.AnimeDTO;
import com.aimanecouissi.animerestapi.payload.response.AnimePaginatedResponse;
import com.aimanecouissi.animerestapi.repository.AnimeRepository;
import com.aimanecouissi.animerestapi.repository.StudioRepository;
import com.aimanecouissi.animerestapi.repository.UserRepository;
import com.aimanecouissi.animerestapi.service.AnimeService;
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
public class AnimeServiceImplementation implements AnimeService {
    private final AnimeRepository animeRepository;
    private final StudioRepository studioRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public AnimeServiceImplementation(AnimeRepository animeRepository, StudioRepository studioRepository, UserRepository userRepository, ModelMapper modelMapper) {
        this.animeRepository = animeRepository;
        this.studioRepository = studioRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public AnimeDTO createAnime(AnimeDTO animeDTO) {
        User currentUser = getCurrentUser();
        if (animeRepository.existsByTitleAndUserId(animeDTO.getTitle(), currentUser.getId())) {
            throw new UniqueFieldException("Title", animeDTO.getTitle());
        }
        Anime anime = toAnime(animeDTO);
        Studio studio = getStudioById(animeDTO.getStudioId());
        anime.setStudio(studio);
        anime.setUser(currentUser);
        return toAnimeDTO(animeRepository.save(anime));
    }

    @Override
    @Transactional(readOnly = true)
    public AnimePaginatedResponse getAllAnime(int pageNo, int pageSize, String sortBy, String sortDir) {
        User currentUser = getCurrentUser();
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.Direction.fromString(sortDir), sortBy);
        Page<Anime> animePage = animeRepository.findAllByUserId(currentUser.getId(), pageRequest);
        return toAnimePaginatedResponse(animePage);
    }

    @Override
    @Transactional(readOnly = true)
    public AnimeDTO getAnimeById(long id) {
        Anime anime = getAnimeByIdAndUser(id, getCurrentUser().getId());
        return toAnimeDTO(anime);
    }

    @Override
    @Transactional
    public AnimeDTO updateAnime(long id, AnimeDTO animeDTO) {
        Anime anime = getAnimeByIdAndUser(id, getCurrentUser().getId());
        if (!anime.getTitle().equals(animeDTO.getTitle()) && animeRepository.existsByTitleAndUserId(animeDTO.getTitle(), anime.getUser().getId())) {
            throw new UniqueFieldException("Title", animeDTO.getTitle());
        }
        Studio studio = getStudioById(animeDTO.getStudioId());
        updateAnimeFields(anime, animeDTO, studio);
        return toAnimeDTO(animeRepository.save(anime));
    }

    @Override
    @Transactional
    public void deleteAnime(long id) {
        Anime anime = getAnimeByIdAndUser(id, getCurrentUser().getId());
        animeRepository.delete(anime);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnimeDTO> getAnimeByStudioId(long studioId) {
        getStudioById(studioId); // Ensure studio exists
        User currentUser = getCurrentUser();
        return animeRepository.findByStudioIdAndUserId(studioId, currentUser.getId())
                .stream()
                .map(this::toAnimeDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnimeDTO> searchAnime(String title, AnimeType type, AnimeStatus status, Integer rating, Boolean isFavorite, Boolean isComplete) {
        return animeRepository.findAllByUserIdAndFilters(
                        getCurrentUser().getId(),
                        title,
                        type,
                        status,
                        rating,
                        isFavorite,
                        isComplete
                ).stream()
                .map(this::toAnimeDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Double getMeanRating() {
        User currentUser = getCurrentUser();
        return animeRepository.findAverageRatingByUserId(currentUser.getId())
                .orElse(0.0);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private Studio getStudioById(long studioId) {
        return studioRepository.findById(studioId)
                .orElseThrow(() -> new ResourceNotFoundException("Studio", "ID", String.valueOf(studioId)));
    }

    private Anime getAnimeByIdAndUser(long animeId, long userId) {
        Anime anime = animeRepository.findById(animeId)
                .orElseThrow(() -> new ResourceNotFoundException("Anime", "ID", String.valueOf(animeId)));
        if (anime.getUser().getId() != userId) {
            throw new UnauthorizedOperationException("You do not have permission to access this anime.");
        }
        return anime;
    }

    private AnimeDTO toAnimeDTO(Anime anime) {
        return modelMapper.map(anime, AnimeDTO.class);
    }

    private Anime toAnime(AnimeDTO animeDTO) {
        return modelMapper.map(animeDTO, Anime.class);
    }

    private void updateAnimeFields(Anime anime, AnimeDTO animeDTO, Studio studio) {
        anime.setTitle(animeDTO.getTitle());
        anime.setType(animeDTO.getType());
        anime.setStudio(studio);
        anime.setStatus(animeDTO.getStatus());
        anime.setRating(animeDTO.getRating());
        anime.setFavorite(animeDTO.isFavorite());
        anime.setComplete(animeDTO.isComplete());
    }

    private AnimePaginatedResponse toAnimePaginatedResponse(Page<Anime> animePage) {
        List<AnimeDTO> animeDTOs = animePage.getContent()
                .stream()
                .map(this::toAnimeDTO)
                .collect(Collectors.toList());
        return AnimePaginatedResponse.builder()
                .items(animeDTOs)
                .pageNumber(animePage.getNumber())
                .pageSize(animePage.getSize())
                .totalPages(animePage.getTotalPages())
                .totalElements(animePage.getTotalElements())
                .isLast(animePage.isLast())
                .build();
    }
}
