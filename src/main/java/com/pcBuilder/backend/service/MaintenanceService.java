package com.pcBuilder.backend.service;

import com.pcBuilder.backend.model.build.BuildEntity;
import com.pcBuilder.backend.repository.BuildRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tareas programadas para mantenimiento y seed de datos.
 */
@Service
public class MaintenanceService {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceService.class);
    private static final long RETENTION_DAYS = 90;

    private final BuildRepository buildRepository;
    private final BuildService buildService;

    public MaintenanceService(BuildRepository buildRepository, BuildService buildService) {
        this.buildRepository = buildRepository;
        this.buildService = buildService;
    }

    /**
     * Limpia builds antiguos para mantener la base liviana.
     */
    @Async
    @Scheduled(cron = "0 30 3 * * *")
    public void cleanOldBuilds() {
        Instant cutoff = Instant.now().minus(Duration.ofDays(RETENTION_DAYS));
        List<BuildEntity> oldBuilds = buildRepository.findAll().stream()
                .filter(b -> b.getCreatedAt() != null && b.getCreatedAt().isBefore(cutoff))
                .collect(Collectors.toList());

        if (!oldBuilds.isEmpty()) {
            buildRepository.deleteAll(oldBuilds);
            log.info("Maintenance: removed {} builds older than {} days", oldBuilds.size(), RETENTION_DAYS);
        } else {
            log.info("Maintenance: no builds to clean");
        }
    }

    /**
     * Recalcula métricas y compatibilidad en lote de forma periódica.
     */
    @Async
    @Scheduled(cron = "0 15 */6 * * *")
    public void recalcBuildMetrics() {
        buildService.recalculateAllBuildsAsync();
    }

    /**
     * Hook para refrescar/sembrar dataset. Implementación placeholder.
     */
    @Async
    @Scheduled(cron = "0 0 4 * * MON")
    public void refreshDataset() {
        log.info("Maintenance: dataset refresh placeholder (implementar si aplica)");
    }
}

