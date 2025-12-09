package com.pcBuilder.backend.controller;

import com.pcBuilder.backend.dto.BuildRequest;
import com.pcBuilder.backend.dto.BuildSummaryDto;
import com.pcBuilder.backend.dto.ComponentSelectionDto;
import com.pcBuilder.backend.dto.PagedResponse;
import com.pcBuilder.backend.mapper.BuildMapper;
import com.pcBuilder.backend.model.build.Build;
import com.pcBuilder.backend.model.component.ComponentCategory;
import com.pcBuilder.backend.service.BuildService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * REST controller for build operations.
 */
@RestController
@RequestMapping("/api/builds")
public class BuildController {

    private final BuildService buildService;
    private final BuildMapper buildMapper;

    public BuildController(BuildService buildService, BuildMapper buildMapper) {
        this.buildService = buildService;
        this.buildMapper = buildMapper;
    }

    /**
     * Create a new build from request.
     */
    @PostMapping
    public ResponseEntity<BuildSummaryDto> createBuild(@Valid @RequestBody BuildRequest request) {
        Build build = buildService.createFromRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(buildMapper.toDto(build));
    }

    /**
     * Get a build by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BuildSummaryDto> getBuild(@PathVariable String id) {
        Build build = buildService.get(id);
        return ResponseEntity.ok(buildMapper.toDto(build));
    }

    /**
     * Get all builds.
     */
    @GetMapping
    public ResponseEntity<List<BuildSummaryDto>> getAll() {
        List<Build> builds = buildService.getAll();
        return ResponseEntity.ok(buildMapper.toDtoList(builds));
    }

    /**
     * Get recent community builds (most recent first) with pagination.
     */
    @GetMapping("/community")
    public ResponseEntity<PagedResponse<BuildSummaryDto>> getCommunityBuilds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        int pageNumber = Math.max(page, 0);
        int pageSize = Math.min(Math.max(size, 1), 50);

        Page<Build> result = buildService.getRecentBuilds(PageRequest.of(pageNumber, pageSize));
        List<BuildSummaryDto> content = buildMapper.toDtoList(result.getContent());

        return ResponseEntity.ok(PagedResponse.from(result, content));
    }

    /**
     * Get recommended builds (admin curated) with pagination.
     */
    @GetMapping("/recommended")
    public ResponseEntity<PagedResponse<BuildSummaryDto>> getRecommended(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {

        int pageNumber = Math.max(page, 0);
        int pageSize = Math.min(Math.max(size, 1), 50);

        Page<Build> result = buildService.getRecommended(PageRequest.of(pageNumber, pageSize));
        List<BuildSummaryDto> content = buildMapper.toDtoList(result.getContent());

        return ResponseEntity.ok(PagedResponse.from(result, content));
    }

    /**
     * Update an existing build.
     */
    @PutMapping("/{id}")
    public ResponseEntity<BuildSummaryDto> updateBuild(
            @PathVariable String id,
            @Valid @RequestBody BuildRequest request) {

        Build build = buildMapper.requestToDomain(request);

        // Resolve components
        request.getComponents().forEach(selection -> {
            ComponentCategory category = ComponentCategory.fromString(selection.getCategory());
            if (category != null) {
                buildService.updateComponent(id, category, selection.getComponentId());
            }
        });

        Build updated = buildService.update(id, build);
        return ResponseEntity.ok(buildMapper.toDto(updated));
    }

    /**
     * Delete a build.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBuild(@PathVariable String id) {
        buildService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Add or update a component in a build.
     */
    @PutMapping("/{id}/components")
    public ResponseEntity<BuildSummaryDto> updateComponent(
            @PathVariable String id,
            @Valid @RequestBody ComponentSelectionDto selection) {

        ComponentCategory category = ComponentCategory.fromString(selection.getCategory());
        if (category == null) {
            return ResponseEntity.badRequest().build();
        }

        Build updated = buildService.updateComponent(id, category, selection.getComponentId());
        return ResponseEntity.ok(buildMapper.toDto(updated));
    }

    /**
     * Remove a component from a build.
     */
    @DeleteMapping("/{id}/components/{category}")
    public ResponseEntity<BuildSummaryDto> removeComponent(
            @PathVariable String id,
            @PathVariable String category) {

        ComponentCategory cat = ComponentCategory.fromString(category);
        if (cat == null) {
            return ResponseEntity.badRequest().build();
        }

        Build updated = buildService.removeComponent(id, cat);
        return ResponseEntity.ok(buildMapper.toDto(updated));
    }

    /**
     * Validate a build's compatibility.
     * Returns the build with updated alerts and recommendations.
     */
    @PostMapping("/{id}/validate")
    public ResponseEntity<BuildSummaryDto> validateBuild(@PathVariable String id) {
        Build build = buildService.get(id);
        Build analyzed = buildService.save(build); // This re-analyzes the build
        return ResponseEntity.ok(buildMapper.toDto(analyzed));
    }

    /**
     * Get compatibility alerts for a build.
     */
    @GetMapping("/{id}/alerts")
    public ResponseEntity<List<String>> getAlerts(@PathVariable String id) {
        Build build = buildService.get(id);
        return ResponseEntity.ok(build.getAlerts());
    }

    /**
     * Get recommendations for a build.
     */
    @GetMapping("/{id}/recommendations")
    public ResponseEntity<List<String>> getRecommendations(@PathVariable String id) {
        Build build = buildService.get(id);
        return ResponseEntity.ok(build.getRecommendations());
    }

    /**
     * Check if a build is complete (has all component categories).
     */
    @GetMapping("/{id}/complete")
    public ResponseEntity<Boolean> isComplete(@PathVariable String id) {
        Build build = buildService.get(id);
        return ResponseEntity.ok(build.isComplete());
    }

    /**
     * Dispara el análisis de compatibilidad en segundo plano.
     */
    @PostMapping("/{id}/analyze-async")
    public ResponseEntity<String> analyzeAsync(@PathVariable String id) {
        buildService.analyzeBuildAsync(id);
        return ResponseEntity.accepted().body("Análisis en proceso para build " + id);
    }

    /**
     * Dispara el recálculo masivo de builds en segundo plano.
     */
    @PostMapping("/recalculate/async")
    public ResponseEntity<String> recalculateAsync() {
        buildService.recalculateAllBuildsAsync();
        return ResponseEntity.accepted().body("Recalculo masivo en proceso");
    }
}
