package com.aimanecouissi.animerestapi.repository;

import com.aimanecouissi.animerestapi.entity.Manga;
import com.aimanecouissi.animerestapi.enums.MangaStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MangaRepository extends JpaRepository<Manga, Long> {
    Optional<Manga> findByTitle(String title);

    boolean existsByTitleAndUserId(String title, long userId);

    Page<Manga> findAllByUserId(long userId, Pageable pageable);

    @Query("SELECT AVG(m.rating) FROM Manga m WHERE m.user.id = :userId")
    Optional<Double> findAverageRatingByUserId(@Param("userId") long userId);

    @Query("SELECT m FROM Manga m WHERE m.user.id = :userId " +
            "AND (:title IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:status IS NULL OR m.status = :status) " +
            "AND (:rating IS NULL OR m.rating = :rating) " +
            "AND (:isFavorite IS NULL OR m.isFavorite = :isFavorite) ")
    List<Manga> findAllByUserIdAndFilters(
            @Param("userId") long userId,
            @Param("title") String title,
            @Param("status") MangaStatus status,
            @Param("rating") Integer rating,
            @Param("isFavorite") Boolean isFavorite
    );
}
