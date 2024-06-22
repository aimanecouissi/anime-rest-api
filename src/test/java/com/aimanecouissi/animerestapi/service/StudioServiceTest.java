package com.aimanecouissi.animerestapi.service;

import com.aimanecouissi.animerestapi.entity.Studio;
import com.aimanecouissi.animerestapi.exception.ResourceNotFoundException;
import com.aimanecouissi.animerestapi.exception.UniqueFieldException;
import com.aimanecouissi.animerestapi.payload.dto.StudioDTO;
import com.aimanecouissi.animerestapi.repository.StudioRepository;
import com.aimanecouissi.animerestapi.service.implementation.StudioServiceImplementation;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(OrderAnnotation.class)
class StudioServiceTest {

    @Mock
    private StudioRepository studioRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private StudioServiceImplementation studioService;

    private Studio studio;
    private StudioDTO studioDTO;

    @BeforeEach
    void setUp() {
        // Studio BO
        studio = Studio.builder()
                .id(1L)
                .name("Madhouse")
                .build();

        // Studio DTO
        studioDTO = StudioDTO.builder()
                .name("Madhouse")
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("Create Studio - Success")
    void shouldCreateStudio() {
        given(studioRepository.existsByName(studio.getName())).willReturn(false);
        given(modelMapper.map(studioDTO, Studio.class)).willReturn(studio);
        given(modelMapper.map(studio, StudioDTO.class)).willReturn(studioDTO);
        given(studioRepository.save(studio)).willReturn(studio);
        StudioDTO savedStudioDTO = studioService.createStudio(studioDTO);
        assertThat(savedStudioDTO).isNotNull();
        assertThat(savedStudioDTO.getName()).isEqualTo(studio.getName());
        verify(studioRepository).save(studio);
    }

    @Test
    @Order(2)
    @DisplayName("Create Studio - Duplicate Name")
    void shouldThrowExceptionWhenCreatingStudioWithDuplicateName() {
        given(studioRepository.existsByName(studio.getName())).willReturn(true);
        UniqueFieldException exception = assertThrows(UniqueFieldException.class, () -> studioService.createStudio(studioDTO));
        assertThat(exception.getMessage()).contains("Name", studioDTO.getName());
    }

    @Test
    @Order(3)
    @DisplayName("Get All Studios - Success")
    void shouldGetAllStudios() {
        Studio anotherStudio = Studio.builder()
                .id(2L)
                .name("Bones")
                .build();
        StudioDTO anotherStudioDTO = StudioDTO.builder()
                .name("Bones")
                .build();
        given(studioRepository.findAll()).willReturn(List.of(studio, anotherStudio));
        given(modelMapper.map(studio, StudioDTO.class)).willReturn(studioDTO);
        given(modelMapper.map(anotherStudio, StudioDTO.class)).willReturn(anotherStudioDTO);
        List<StudioDTO> studioDTOs = studioService.getAllStudios();
        assertThat(studioDTOs).isNotEmpty();
        assertThat(studioDTOs.size()).isEqualTo(2);
        assertThat(studioDTOs).contains(studioDTO, anotherStudioDTO);
    }

    @Test
    @Order(4)
    @DisplayName("Get All Studios - Empty List")
    void shouldReturnEmptyListWhenGetAllStudios() {
        given(studioRepository.findAll()).willReturn(Collections.emptyList());
        List<StudioDTO> studioDTOs = studioService.getAllStudios();
        assertThat(studioDTOs).isEmpty();
    }

    @Test
    @Order(5)
    @DisplayName("Get Studio By ID - Success")
    void shouldGetStudioById() {
        given(studioRepository.findById(1L)).willReturn(Optional.of(studio));
        given(modelMapper.map(studio, StudioDTO.class)).willReturn(studioDTO);
        StudioDTO foundStudioDTO = studioService.getStudioById(1L);
        assertThat(foundStudioDTO).isNotNull();
        assertThat(foundStudioDTO.getName()).isEqualTo(studio.getName());
    }

    @Test
    @Order(6)
    @DisplayName("Get Studio By ID - Not Found")
    void shouldThrowExceptionWhenStudioNotFoundById() {
        given(studioRepository.findById(1L)).willReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> studioService.getStudioById(1L));
        assertThat(exception.getMessage()).contains("Studio", "ID", "1");
    }

    @Test
    @Order(7)
    @DisplayName("Update Studio - Success")
    void shouldUpdateStudio() {
        Studio existingStudio = Studio.builder()
                .id(1L)
                .name("Existing Studio")
                .build();
        Studio updatedStudio = Studio.builder()
                .id(1L)
                .name("MAPPA")
                .build();
        StudioDTO updatedStudioDTO = StudioDTO.builder()
                .name("MAPPA")
                .build();
        given(studioRepository.findById(1L)).willReturn(Optional.of(existingStudio));
        given(studioRepository.existsByName(updatedStudioDTO.getName())).willReturn(false);
        doReturn(updatedStudioDTO).when(modelMapper).map(updatedStudio, StudioDTO.class);
        doReturn(updatedStudio).when(studioRepository).save(existingStudio);
        StudioDTO savedUpdatedStudioDTO = studioService.updateStudio(1L, updatedStudioDTO);
        assertThat(savedUpdatedStudioDTO).isNotNull();
        assertThat(savedUpdatedStudioDTO.getName()).isEqualTo(updatedStudio.getName());
        verify(studioRepository).save(existingStudio);
    }

    @Test
    @Order(8)
    @DisplayName("Update Studio - Duplicate Name")
    void shouldThrowExceptionWhenUpdatingStudioWithDuplicateName() {
        StudioDTO updatedStudioDTO = StudioDTO.builder()
                .name("Bones")
                .build();
        given(studioRepository.findById(1L)).willReturn(Optional.of(studio));
        given(studioRepository.existsByName(updatedStudioDTO.getName())).willReturn(true);
        UniqueFieldException exception = assertThrows(UniqueFieldException.class, () -> studioService.updateStudio(1L, updatedStudioDTO));
        assertThat(exception.getMessage()).contains("Name", updatedStudioDTO.getName());
    }

    @Test
    @Order(9)
    @DisplayName("Delete Studio - Success")
    void shouldDeleteStudio() {
        given(studioRepository.findById(1L)).willReturn(Optional.of(studio));
        willDoNothing().given(studioRepository).delete(studio);
        studioService.deleteStudio(1L);
        verify(studioRepository).delete(studio);
    }

    @Test
    @Order(10)
    @DisplayName("Delete Studio - Not Found")
    void shouldThrowExceptionWhenDeletingNonExistentStudio() {
        given(studioRepository.findById(1L)).willReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> studioService.deleteStudio(1L));
        assertThat(exception.getMessage()).contains("Studio", "ID", "1");
    }
}
