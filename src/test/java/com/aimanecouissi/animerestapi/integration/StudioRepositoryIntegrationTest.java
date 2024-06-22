package com.aimanecouissi.animerestapi.integration;

import com.aimanecouissi.animerestapi.entity.Studio;
import com.aimanecouissi.animerestapi.repository.StudioRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(OrderAnnotation.class)
class StudioRepositoryIntegrationTest {

    @Autowired
    private StudioRepository studioRepository;

    private Studio studio;

    @BeforeEach
    void setUp() {
        studioRepository.deleteAll();
        studio = Studio.builder().name("Studio Ghibli").build();
        studioRepository.save(studio);
    }

    @Test
    @Order(1)
    @DisplayName("Save Studio")
    void shouldSaveStudio() {
        Studio newStudio = Studio.builder().name("Madhouse").build();
        Studio savedStudio = studioRepository.save(newStudio);
        assertThat(savedStudio).isNotNull();
        assertThat(savedStudio.getId()).isGreaterThan(0);
        assertThat(savedStudio.getName()).isEqualTo("Madhouse");
    }

    @Test
    @Order(2)
    @DisplayName("Find All Studios")
    void shouldFindAllStudios() {
        Studio studio1 = Studio.builder().name("Bones").build();
        studioRepository.save(studio1);
        Studio studio2 = Studio.builder().name("Sunrise").build();
        studioRepository.save(studio2);
        List<Studio> studios = studioRepository.findAll();
        assertThat(studios.size()).isEqualTo(3);
        assertThat(studios).contains(studio, studio1, studio2);
    }

    @Test
    @Order(3)
    @DisplayName("Find Studio By ID")
    void shouldFindStudioById() {
        Optional<Studio> foundStudio = studioRepository.findById(studio.getId());
        assertThat(foundStudio.isPresent()).isTrue();
        assertThat(foundStudio.get().getName()).isEqualTo("Studio Ghibli");
    }

    @Test
    @Order(4)
    @DisplayName("Update Studio")
    void shouldUpdateStudio() {
        studio.setName("MAPPA");
        Studio updatedStudio = studioRepository.save(studio);
        assertThat(updatedStudio.getName()).isEqualTo("MAPPA");
    }

    @Test
    @Order(5)
    @DisplayName("Find Studio By Name")
    void shouldFindStudioByName() {
        Optional<Studio> foundStudio = studioRepository.findByName(studio.getName());
        assertThat(foundStudio.isPresent()).isTrue();
        assertThat(foundStudio.get().getName()).isEqualTo("Studio Ghibli");
    }

    @Test
    @Order(7)
    @DisplayName("Check Studio Existence By Name")
    void shouldCheckStudioExistenceByName() {
        boolean exists = studioRepository.existsByName(studio.getName());
        assertThat(exists).isTrue();
    }

    @Test
    @Order(6)
    @DisplayName("Delete Studio")
    void shouldDeleteStudio() {
        studioRepository.delete(studio);
        List<Studio> studios = studioRepository.findAll();
        assertThat(studios.size()).isEqualTo(0);
    }
}
