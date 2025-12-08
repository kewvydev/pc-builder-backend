package com.pcBuilder.backend.repository;

import com.pcBuilder.backend.model.scraping.ScrapingLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for ScrapingLogEntity database operations.
 */
@Repository
public interface ScrapingLogRepository extends JpaRepository<ScrapingLogEntity, Long> {

    /**
     * Find logs by category.
     */
    List<ScrapingLogEntity> findByCategory(String category);

    /**
     * Find logs by status.
     */
    List<ScrapingLogEntity> findByStatus(String status);

    /**
     * Find logs created after a specific date.
     */
    List<ScrapingLogEntity> findByCreatedAtAfter(Instant date);

    /**
     * Find logs by category and status.
     */
    List<ScrapingLogEntity> findByCategoryAndStatus(String category, String status);

    /**
     * Find recent logs ordered by creation date descending.
     */
    @Query("SELECT s FROM ScrapingLogEntity s ORDER BY s.createdAt DESC")
    List<ScrapingLogEntity> findRecentLogs();

    /**
     * Find logs with limit.
     */
    @Query(value = "SELECT * FROM scraping_logs ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
    List<ScrapingLogEntity> findRecentLogsWithLimit(@Param("limit") int limit);
}

