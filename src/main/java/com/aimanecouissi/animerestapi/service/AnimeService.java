package com.aimanecouissi.animerestapi.service;

import com.aimanecouissi.animerestapi.enums.AnimeStatus;
import com.aimanecouissi.animerestapi.enums.AnimeType;
import com.aimanecouissi.animerestapi.payload.dto.AnimeDTO;
import com.aimanecouissi.animerestapi.payload.response.AnimePaginatedResponse;

import java.util.List;

public interface AnimeService {
    AnimeDTO createAnime(AnimeDTO animeDTO);

    AnimePaginatedResponse getAllAnime(int pageNo, int pageSize, String sortBy, String sortDir);

    AnimeDTO getAnimeById(long id);

    AnimeDTO updateAnime(long id, AnimeDTO animeDTO);

    void deleteAnime(long id);

    List<AnimeDTO> getAnimeByStudioId(long studioId);

    List<AnimeDTO> searchAnime(String title, AnimeType type, AnimeStatus status, Integer rating, Boolean isFavorite, Boolean isComplete);

    Double getMeanRating();
}
