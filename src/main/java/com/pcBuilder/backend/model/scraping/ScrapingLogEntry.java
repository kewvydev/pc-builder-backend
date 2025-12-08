package com.pcBuilder.backend.model.scraping;

import com.pcBuilder.backend.model.component.ComponentCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScrapingLogEntry {

    @Builder.Default
    private Instant timestamp = Instant.now();
    private ComponentCategory category;
    private String status;
    private long durationMs;
    private int itemsFound;
    private String message;
}
