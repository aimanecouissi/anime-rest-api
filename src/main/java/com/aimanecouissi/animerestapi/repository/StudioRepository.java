package com.aimanecouissi.animerestapi.repository;

import com.aimanecouissi.animerestapi.entity.Studio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudioRepository extends JpaRepository<Studio, Long> {
    boolean existsByName(String name);

    Optional<Studio> findByName(String name);
}
