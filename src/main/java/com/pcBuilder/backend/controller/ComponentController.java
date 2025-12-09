package com.pcBuilder.backend.controller;

import com.pcBuilder.backend.dto.ComponentDto;
import com.pcBuilder.backend.dto.ComponentListDto;
import com.pcBuilder.backend.dto.PagedResponse;
import com.pcBuilder.backend.mapper.ComponentMapper;
import com.pcBuilder.backend.model.component.Component;
import com.pcBuilder.backend.service.ComponentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for component operations.
 * 
 * PERFORMANCE NOTES:
 * - Use /v2/ endpoints for optimized, paginated responses (RECOMMENDED)
 * - Legacy endpoints (/api/components/*) are kept for backward compatibility
 * - For large datasets, always use paginated endpoints to avoid timeouts
 */
@RestController
@RequestMapping("/api/components")
public class ComponentController {

    private static final int DEFAULT_PAGE_SIZE = 50;

    private final ComponentService componentService;
    private final ComponentMapper componentMapper;

    public ComponentController(ComponentService componentService, ComponentMapper componentMapper) {
        this.componentService = componentService;
        this.componentMapper = componentMapper;
    }

    // ==================== OPTIMIZED V2 ENDPOINTS (RECOMMENDED)
    // ====================
    // These return lightweight DTOs with pagination for best performance

    /**
     * Get components by category with pagination (OPTIMIZED).
     * Returns lightweight DTOs without attributes/tags for fast loading.
     * 
     * Example: GET /api/components/v2/cpu?page=0&size=50&sortBy=price&sortDir=asc
     */
    @GetMapping("/v2/{category}")
    public ResponseEntity<PagedResponse<ComponentListDto>> getByTypePaginated(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Page<ComponentListDto> result = componentService.getByTypeLightweight(category, page, size, sortBy, sortDir);
        return ResponseEntity.ok(PagedResponse.from(result));
    }

    /**
     * Get ALL components by category without pagination (OPTIMIZED).
     * Returns lightweight DTOs without attributes/tags.
     * Use this only when you need all items at once.
     * 
     * Example: GET /api/components/v2/cpu/all
     */
    @GetMapping("/v2/{category}/all")
    public ResponseEntity<List<ComponentListDto>> getByTypeAllLightweight(@PathVariable String category) {
        List<ComponentListDto> components = componentService.getByTypeLightweight(category);
        return ResponseEntity.ok(components);
    }

    /**
     * Get all components with pagination (OPTIMIZED).
     * 
     * Example: GET /api/components/v2?page=0&size=50
     */
    @GetMapping("/v2")
    public ResponseEntity<PagedResponse<ComponentListDto>> getAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Page<ComponentListDto> result = componentService.getAllLightweight(page, size, sortBy, sortDir);
        return ResponseEntity.ok(PagedResponse.from(result));
    }

    /**
     * Search components by name with pagination (OPTIMIZED).
     * 
     * Example: GET /api/components/v2/search?query=ryzen&page=0&size=50
     */
    @GetMapping("/v2/search")
    public ResponseEntity<PagedResponse<ComponentListDto>> searchPaginated(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Page<ComponentListDto> result = componentService.searchByNameLightweight(query, page, size);
        return ResponseEntity.ok(PagedResponse.from(result));
    }

    /**
     * Search components by name and category with pagination (OPTIMIZED).
     * 
     * Example: GET /api/components/v2/search/cpu?query=ryzen&page=0&size=50
     */
    @GetMapping("/v2/search/{category}")
    public ResponseEntity<PagedResponse<ComponentListDto>> searchByCategoryPaginated(
            @PathVariable String category,
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Page<ComponentListDto> result = componentService.searchByNameAndCategoryLightweight(query, category, page,
                size);
        return ResponseEntity.ok(PagedResponse.from(result));
    }

    /**
     * Filter components by category and price range with pagination (OPTIMIZED).
     * 
     * Example: GET
     * /api/components/v2/cpu/filter?minPrice=100&maxPrice=500&page=0&size=50
     */
    @GetMapping("/v2/{category}/filter")
    public ResponseEntity<PagedResponse<ComponentListDto>> filterByCategoryPaginated(
            @PathVariable String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        if (minPrice != null && maxPrice != null) {
            Page<ComponentListDto> result = componentService.findByCategoryAndPriceRangeLightweight(
                    category, minPrice, maxPrice, page, size);
            return ResponseEntity.ok(PagedResponse.from(result));
        } else {
            Page<ComponentListDto> result = componentService.getByTypeLightweight(category, page, size, "price", "asc");
            return ResponseEntity.ok(PagedResponse.from(result));
        }
    }

    /**
     * Get component by ID with FULL details (includes attributes and tags).
     * Use this when you need the complete component information.
     * 
     * Example: GET /api/components/v2/detail/cpu-12345
     */
    @GetMapping("/v2/detail/{id}")
    public ResponseEntity<ComponentDto> getByIdFull(@PathVariable String id) {
        Component component = componentService.getByIdWithFullDetails(id);
        return ResponseEntity.ok(componentMapper.toDto(component));
    }

    /**
     * Get distinct brands by category.
     * 
     * Example: GET /api/components/v2/cpu/brands
     */
    @GetMapping("/v2/{category}/brands")
    public ResponseEntity<List<String>> getBrandsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(componentService.getBrandsByCategory(category));
    }

    // ==================== LEGACY ENDPOINTS (BACKWARD COMPATIBILITY)
    // ====================
    // These are kept for backward compatibility but may be slow for large datasets

    /**
     * Get all components.
     * WARNING: This can be slow for large datasets. Use /v2 endpoints instead.
     */
    @GetMapping
    public ResponseEntity<List<ComponentDto>> getAll() {
        List<Component> components = componentService.getAll();
        return ResponseEntity.ok(componentMapper.toDtoList(components));
    }

    /**
     * Get components by category/type.
     * WARNING: This can be slow for large datasets. Use /v2/{category} instead.
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
