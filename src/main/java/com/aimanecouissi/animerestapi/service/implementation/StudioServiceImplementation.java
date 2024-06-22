package com.aimanecouissi.animerestapi.service.implementation;

import com.aimanecouissi.animerestapi.entity.Studio;
import com.aimanecouissi.animerestapi.exception.ResourceNotFoundException;
import com.aimanecouissi.animerestapi.exception.UniqueFieldException;
import com.aimanecouissi.animerestapi.payload.dto.StudioDTO;
import com.aimanecouissi.animerestapi.repository.StudioRepository;
import com.aimanecouissi.animerestapi.service.StudioService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudioServiceImplementation implements StudioService {
    private final StudioRepository studioRepository;
    private final ModelMapper modelMapper;

    public StudioServiceImplementation(StudioRepository studioRepository, ModelMapper modelMapper) {
        this.studioRepository = studioRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public StudioDTO createStudio(StudioDTO studioDTO) {
        if (studioRepository.existsByName(studioDTO.getName())) {
            throw new UniqueFieldException("Name", studioDTO.getName());
        }
        Studio studio = toStudio(studioDTO);
        return toStudioDTO(studioRepository.save(studio));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudioDTO> getAllStudios() {
        return studioRepository.findAll()
                .stream()
                .map(this::toStudioDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StudioDTO getStudioById(long id) {
        Studio studio = getStudioByIdOrThrow(id);
        return toStudioDTO(studio);
    }

    @Override
    @Transactional
    public StudioDTO updateStudio(long id, StudioDTO studioDTO) {
        Studio studio = getStudioByIdOrThrow(id);
        if (studioRepository.existsByName(studioDTO.getName()) && !studio.getName().equals(studioDTO.getName())) {
            throw new UniqueFieldException("Name", studioDTO.getName());
        }
        studio.setName(studioDTO.getName());
        return toStudioDTO(studioRepository.save(studio));
    }

    @Override
    @Transactional
    public void deleteStudio(long id) {
        Studio studio = getStudioByIdOrThrow(id);
        studioRepository.delete(studio);
    }

    private Studio getStudioByIdOrThrow(long id) {
        return studioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Studio", "ID", String.valueOf(id)));
    }

    private StudioDTO toStudioDTO(Studio studio) {
        return modelMapper.map(studio, StudioDTO.class);
    }

    private Studio toStudio(StudioDTO studioDTO) {
        return modelMapper.map(studioDTO, Studio.class);
    }
}
