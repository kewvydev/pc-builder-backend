package com.pcBuilder.backend.repository;

import com.pcBuilder.backend.model.build.BuildEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for BuildEntity database operations.
 */
@Repository
public interface BuildRepository extends JpaRepository<BuildEntity, UUID> {

    /**
     * Find builds by name containing text (case insensitive).
     */
    @Query("SELECT b FROM BuildEntity b WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<BuildEntity> searchByName(@Param("searchText") String searchText);

    /**
     * Find builds with budget less than or equal to specified amount.
     */
    List<BuildEntity> findByBudgetLessThanEqual(BigDecimal maxBudget);

    /**
     * Find builds created after a specific date.
     */
    List<BuildEntity> findByCreatedAtAfter(Instant date);

    /**
     * Find builds ordered by creation date descending (most recent first).
     */
    List<BuildEntity> findAllByOrderByCreatedAtDesc();

    /**
     * Find builds ordered by total price ascending.
     */
    List<BuildEntity> findAllByOrderByTotalPriceAsc();

    /**
     * Count total number of builds.
     */
    long count();
}

