package com.pcBuilder.backend.model.scraping;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * JPA Entity for scraping operation logs.
 */
@Entity
@Table(name = "scraping_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScrapingLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "duration_ms")
    private Integer durationMs;

    @Column(name = "items_found")
    private Integer itemsFound;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}

