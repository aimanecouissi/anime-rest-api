package com.aimanecouissi.animerestapi.service;

import com.aimanecouissi.animerestapi.enums.MangaStatus;
import com.aimanecouissi.animerestapi.payload.dto.MangaDTO;
import com.aimanecouissi.animerestapi.payload.response.MangaPaginatedResponse;

import java.util.List;

public interface MangaService {
    MangaDTO createManga(MangaDTO mangaDTO);

    MangaPaginatedResponse getAllManga(int pageNo, int pageSize, String sortBy, String sortDir);

    MangaDTO getMangaById(long id);

    MangaDTO updateManga(long id, MangaDTO mangaDTO);

    void deleteManga(long id);

    List<MangaDTO> searchManga(String title, MangaStatus status, Integer rating, Boolean isFavorite);

    Double getMeanRating();
}
