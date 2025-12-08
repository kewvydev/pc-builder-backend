package com.pcBuilder.backend.controller;

import com.pcBuilder.backend.dto.ComponentDto;
import com.pcBuilder.backend.mapper.ComponentMapper;
import com.pcBuilder.backend.model.component.Component;
import com.pcBuilder.backend.service.ComponentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for component operations.
 */
@RestController
@RequestMapping("/api/components")
public class ComponentController {

    private final ComponentService componentService;
    private final ComponentMapper componentMapper;

    public ComponentController(ComponentService componentService, ComponentMapper componentMapper) {
        this.componentService = componentService;
        this.componentMapper = componentMapper;
    }

    /**
     * Get all components.
     */
    @GetMapping
    public ResponseEntity<List<ComponentDto>> getAll() {
        List<Component> components = componentService.getAll();
        return ResponseEntity.ok(componentMapper.toDtoList(components));
    }

    /**
     * Get components by category/type.
     */
    @GetMapping("/{type}")
    public ResponseEntity<List<ComponentDto>> getByType(@PathVariable String type) {
        List<Component> components = componentService.getByType(type);
        return ResponseEntity.ok(componentMapper.toDtoList(components));
    }

    /**
     * Get a specific component by type and id.
     */
    @GetMapping("/{type}/{id}")
    public ResponseEntity<ComponentDto> getOne(
            @PathVariable String type,
            @PathVariable String id) {
        Component component = componentService.getOne(type, id);
        return ResponseEntity.ok(componentMapper.toDto(component));
    }

    /**
     * Get component by ID (without type validation).
     */
    @GetMapping("/id/{id}")
    public ResponseEntity<ComponentDto> getById(@PathVariable String id) {
        Component component = componentService.getById(id);
        return ResponseEntity.ok(componentMapper.toDto(component));
    }

    /**
     * Create a new component.
     */
    @PostMapping
    public ResponseEntity<ComponentDto> create(@Valid @RequestBody ComponentDto componentDto) {
        Component component = componentMapper.toDomain(componentDto);
        Component saved = componentService.save(component);
        return ResponseEntity.status(HttpStatus.CREATED).body(componentMapper.toDto(saved));
    }

    /**
     * Update an existing component.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ComponentDto> update(
            @PathVariable String id,
            @Valid @RequestBody ComponentDto componentDto) {
        Component component = componentMapper.toDomain(componentDto);
        Component updated = componentService.update(id, component);
        return ResponseEntity.ok(componentMapper.toDto(updated));
    }

    /**
     * Delete a component.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        componentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search components by name.
     */
    @GetMapping("/search")
    public ResponseEntity<List<ComponentDto>> search(@RequestParam String query) {
        List<Component> components = componentService.searchByName(query);
        return ResponseEntity.ok(componentMapper.toDtoList(components));
    }

    /**
     * Search components by name and category.
     */
    @GetMapping("/search/{category}")
    public ResponseEntity<List<ComponentDto>> searchByCategory(
            @PathVariable String category,
            @RequestParam String query) {
        List<Component> components = componentService.searchByNameAndCategory(query, category);
        return ResponseEntity.ok(componentMapper.toDtoList(components));
    }

    /**
     * Filter components by various criteria.
     */
    @GetMapping("/filter")
    public ResponseEntity<List<ComponentDto>> filter(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        List<Component> components;

        if (minPrice != null && maxPrice != null) {
            components = componentService.findByPriceRange(minPrice, maxPrice);
        } else if (brand != null) {
            components = componentService.findByBrand(brand);
        } else if (tag != null) {
            components = componentService.findByTag(tag);
        } else if (Boolean.TRUE.equals(inStock)) {
            components = componentService.findInStock();
        } else {
            components = componentService.getAll();
        }

        return ResponseEntity.ok(componentMapper.toDtoList(components));
    }

    /**
     * Filter components by category and price range.
     */
    @GetMapping("/{category}/filter")
    public ResponseEntity<List<ComponentDto>> filterByCategory(
            @PathVariable String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        List<Component> components;

        if (minPrice != null && maxPrice != null) {
            components = componentService.findByCategoryAndPriceRange(category, minPrice, maxPrice);
        } else {
            components = componentService.getByType(category);
        }

        return ResponseEntity.ok(componentMapper.toDtoList(components));
    }

    /**
     * Get all distinct brands.
     */
    @GetMapping("/brands")
    public ResponseEntity<List<String>> getAllBrands() {
        return ResponseEntity.ok(componentService.getAllBrands());
    }

    /**
     * Get component count by category.
     */
    @GetMapping("/{category}/count")
    public ResponseEntity<Long> countByCategory(@PathVariable String category) {
        return ResponseEntity.ok(componentService.countByCategory(category));
    }

    /**
     * Get total component count.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> count() {
        return ResponseEntity.ok(componentService.count());
    }

    /**
     * Bulk create components.
     */
    @PostMapping("/bulk")
    public ResponseEntity<List<ComponentDto>> bulkCreate(
            @Valid @RequestBody List<ComponentDto> componentDtos) {
        List<Component> components = componentMapper.toDomainList(componentDtos);
        List<Component> saved = componentService.saveAll(components);
        return ResponseEntity.status(HttpStatus.CREATED).body(componentMapper.toDtoList(saved));
    }
}
