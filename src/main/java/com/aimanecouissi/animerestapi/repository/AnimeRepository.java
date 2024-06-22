package com.aimanecouissi.animerestapi.repository;

import com.aimanecouissi.animerestapi.entity.Anime;
import com.aimanecouissi.animerestapi.enums.AnimeStatus;
import com.aimanecouissi.animerestapi.enums.AnimeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnimeRepository extends JpaRepository<Anime, Long> {
    Optional<Anime> findByTitle(String title);

    boolean existsByTitleAndUserId(String title, long userId);

    List<Anime> findByStudioIdAndUserId(long studioId, long userId);

    Page<Anime> findAllByUserId(long userId, Pageable pageable);

    @Query("SELECT AVG(a.rating) FROM Anime a WHERE a.user.id = :userId")
    Optional<Double> findAverageRatingByUserId(@Param("userId") long userId);

    @Query("SELECT a FROM Anime a WHERE a.user.id = :userId " +
            "AND (:title IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:type IS NULL OR a.type = :type) " +
            "AND (:status IS NULL OR a.status = :status) " +
            "AND (:rating IS NULL OR a.rating = :rating) " +
            "AND (:isFavorite IS NULL OR a.isFavorite = :isFavorite) " +
            "AND (:isComplete IS NULL OR a.isComplete = :isComplete)")
    List<Anime> findAllByUserIdAndFilters(
            @Param("userId") long userId,
            @Param("title") String title,
            @Param("type") AnimeType type,
            @Param("status") AnimeStatus status,
            @Param("rating") Integer rating,
            @Param("isFavorite") Boolean isFavorite,
            @Param("isComplete") Boolean isComplete
    );
}
