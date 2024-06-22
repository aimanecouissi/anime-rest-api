package com.aimanecouissi.animerestapi.service;

import com.aimanecouissi.animerestapi.payload.dto.StudioDTO;

import java.util.List;

public interface StudioService {
    StudioDTO createStudio(StudioDTO studioDTO);

    List<StudioDTO> getAllStudios();

    StudioDTO getStudioById(long id);

    StudioDTO updateStudio(long id, StudioDTO studioDTO);

    void deleteStudio(long id);
}
