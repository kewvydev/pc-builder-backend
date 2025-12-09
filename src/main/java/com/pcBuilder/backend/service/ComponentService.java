package com.pcBuilder.backend.service;

import com.pcBuilder.backend.dto.ComponentListDto;
import com.pcBuilder.backend.exception.InvalidCategoryException;
import com.pcBuilder.backend.exception.ResourceNotFoundException;
import com.pcBuilder.backend.model.component.Component;
import com.pcBuilder.backend.model.component.ComponentCategory;
import com.pcBuilder.backend.model.component.ComponentEntity;
import com.pcBuilder.backend.repository.ComponentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing components using PostgreSQL database.
 */
@Service
@Transactional
public class ComponentService {

    private static final Logger log = LoggerFactory.getLogger(ComponentService.class);
    private static final int MAX_PAGE_SIZE = 200;

    private final ComponentRepository componentRepository;

    public ComponentService(ComponentRepository componentRepository) {
        this.componentRepository = componentRepository;
    }

    // ==================== OPTIMIZED LIGHTWEIGHT METHODS ====================
    // Use these for listing/browsing - they return lightweight DTOs

    /**
     * Get components by category with lightweight DTO (FASTEST - no
     * attributes/tags).
     * This is the recommended method for listing components.
     */
    @Transactional(readOnly = true)
    public List<ComponentListDto> getByTypeLightweight(String type) {
        ComponentCategory category = ComponentCategory.fromString(type);
        if (category == null) {
            throw new InvalidCategoryException(type);
        }
        return componentRepository.findByCategoryLightweight(category.name());
    }

    /**
     * Get components by category with pagination (lightweight DTO).
     */
    @Transactional(readOnly = true)
    public Page<ComponentListDto> getByTypeLightweight(String type, int page, int size, String sortBy, String sortDir) {
        ComponentCategory category = ComponentCategory.fromString(type);
        if (category == null) {
            throw new InvalidCategoryException(type);
        }
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        return componentRepository.findByCategoryLightweight(category.name(), pageable);
    }

    /**
     * Get all components with pagination (lightweight DTO).
     */
    @Transactional(readOnly = true)
    public Page<ComponentListDto> getAllLightweight(int page, int size, String sortBy, String sortDir) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        return componentRepository.findAllLightweight(pageable);
    }

    /**
     * Search components by name with pagination (lightweight DTO).
     */
    @Transactional(readOnly = true)
    public Page<ComponentListDto> searchByNameLightweight(String searchText, int page, int size) {
        Pageable pageable = createPageable(page, size, "name", "asc");
        return componentRepository.searchByNameLightweight(searchText, pageable);
    }

    /**
     * Search components by name and category with pagination (lightweight DTO).
     */
    @Transactional(readOnly = true)
    public Page<ComponentListDto> searchByNameAndCategoryLightweight(String searchText, String category, int page,
            int size) {
        ComponentCategory cat = ComponentCategory.fromString(category);
        if (cat == null) {
            throw new InvalidCategoryException(category);
        }
        Pageable pageable = createPageable(page, size, "name", "asc");
        return componentRepository.searchByNameAndCategoryLightweight(searchText, cat.name(), pageable);
    }

    /**
     * Find components by category and price range with pagination (lightweight
     * DTO).
     */
    @Transactional(readOnly = true)
    public Page<ComponentListDto> findByCategoryAndPriceRangeLightweight(String category, double minPrice,
            double maxPrice, int page, int size) {
        ComponentCategory cat = ComponentCategory.fromString(category);
        if (cat == null) {
            throw new InvalidCategoryException(category);
        }
        Pageable pageable = createPageable(page, size, "price", "asc");
        return componentRepository.findByCategoryAndPriceBetweenLightweight(
                cat.name(),
                BigDecimal.valueOf(minPrice),
                BigDecimal.valueOf(maxPrice),
                pageable);
    }

    /**
     * Find components by brand with pagination (lightweight DTO).
     */
    @Transactional(readOnly = true)
    public Page<ComponentListDto> findByBrandLightweight(String brand, int page, int size) {
        Pageable pageable = createPageable(page, size, "name", "asc");
        return componentRepository.findByBrandLightweight(brand, pageable);
    }

    /**
     * Find components in stock with pagination (lightweight DTO).
     */
    @Transactional(readOnly = true)
    public Page<ComponentListDto> findInStockLightweight(int page, int size) {
        Pageable pageable = createPageable(page, size, "name", "asc");
        return componentRepository.findByInStockTrueLightweight(pageable);
    }

    // ==================== FULL ENTITY METHODS ====================
    // Use these when you need the complete component with attributes/tags

    /**
     * Get all components from the database (DEPRECATED for large datasets).
     * Prefer getAllLightweight() for listings.
     */
    @Transactional(readOnly = true)
    public List<Component> getAll() {
        return componentRepository.findAll().stream()
                .map(ComponentEntity::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Get components by category/type with full details (attributes + tags).
     * Uses optimized two-query approach to avoid Cartesian Product.
     * For listings without attributes, prefer getByTypeLightweight().
     */
    @Transactional(readOnly = true)
    public List<Component> getByType(String type) {
        ComponentCategory category = ComponentCategory.fromString(type);
        if (category == null) {
            throw new InvalidCategoryException(type);
        }

        // Use optimized two-query approach to avoid Cartesian Product
        // This is MUCH faster than a single query with EntityGraph on multiple
        // collections
        List<ComponentEntity> entities = componentRepository.findByCategoryWithAttributes(category.name());

        if (entities.isEmpty()) {
            return List.of();
        }

        // Second query: get tags for these components
        List<String> ids = entities.stream().map(ComponentEntity::getId).collect(Collectors.toList());
        List<ComponentEntity> entitiesWithTags = componentRepository.findByIdsWithTags(ids);

        // Merge tags into entities
        Map<String, ComponentEntity> tagMap = entitiesWithTags.stream()
                .collect(Collectors.toMap(ComponentEntity::getId, e -> e));

        return entities.stream()
                .map(entity -> {
                    ComponentEntity withTags = tagMap.get(entity.getId());
                    if (withTags != null) {
                        entity.setTags(withTags.getTags());
                    }
                    return entity.toDomain();
                })
                .collect(Collectors.toList());
    }

    /**
     * Get a specific component by type and id.
     */
    @Transactional(readOnly = true)
    public Component getOne(String type, String id) {
        ComponentCategory category = ComponentCategory.fromString(type);
        if (category == null) {
            throw new InvalidCategoryException(type);
        }

        ComponentEntity entity = componentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Component", id));

        if (!entity.getCategory().equals(category.name())) {
            throw new ResourceNotFoundException(
                    String.format("Component with id %s not found in category %s", id, type));
        }

        return entity.toDomain();
    }

    /**
     * Get component by ID with full details (attributes + tags).
     */
    @Transactional(readOnly = true)
    public Component getByIdWithFullDetails(String id) {
        // Get with attributes first
        ComponentEntity entity = componentRepository.findByIdWithAttributes(id)
                .orElseThrow(() -> new ResourceNotFoundException("Component", id));

        // Get tags separately
        componentRepository.findByIdWithTags(id)
                .ifPresent(e -> entity.setTags(e.getTags()));

        return entity.toDomain();
    }

    /**
     * Get component by ID.
     */
    @Transactional(readOnly = true)
    public Component getById(String id) {
        return componentRepository.findById(id)
                .map(ComponentEntity::toDomain)
                .orElseThrow(() -> new ResourceNotFoundException("Component", id));
    }

    /**
     * Save a component to the database.
     */
    public Component save(Component component) {
        ComponentEntity entity = ComponentEntity.fromDomain(component);
        ComponentEntity saved = componentRepository.save(entity);
        log.info("Saved component: {} ({})", saved.getName(), saved.getId());
        return saved.toDomain();
    }

    /**
     * Save multiple components at once.
     */
    public List<Component> saveAll(List<Component> components) {
        List<ComponentEntity> entities = components.stream()
                .map(ComponentEntity::fromDomain)
                .collect(Collectors.toList());

        List<ComponentEntity> saved = componentRepository.saveAll(entities);
        log.info("Saved {} components to database", saved.size());

        return saved.stream()
                .map(ComponentEntity::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Delete a component by ID.
     */
    public void delete(String id) {
        if (!componentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Component", id);
        }
        componentRepository.deleteById(id);
        log.info("Deleted component: {}", id);
    }

    /**
     * Update an existing component.
     */
    public Component update(String id, Component component) {
        if (!componentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Component", id);
        }
        component.setId(id);
        ComponentEntity entity = ComponentEntity.fromDomain(component);
        ComponentEntity updated = componentRepository.save(entity);
        log.info("Updated component: {} ({})", updated.getName(), updated.getId());
        return updated.toDomain();
    }

    /**
     * Get all distinct brands (optimized query).
     */
    @Transactional(readOnly = true)
    public List<String> getAllBrands() {
        return componentRepository.findAllDistinctBrands();
    }

    /**
     * Get all distinct brands by category.
     */
    @Transactional(readOnly = true)
    public List<String> getBrandsByCategory(String category) {
        ComponentCategory cat = ComponentCategory.fromString(category);
        if (cat == null) {
            throw new InvalidCategoryException(category);
        }
        return componentRepository.findDistinctBrandsByCategory(cat.name());
    }

    /**
     * Search components by name.
     */
    @Transactional(readOnly = true)
    public List<Component> searchByName(String searchText) {
        return componentRepository.searchByName(searchText).stream()
                .map(ComponentEntity::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Search components by name and category.
     */
    @Transactional(readOnly = true)
    public List<Component> searchByNameAndCategory(String searchText, String category) {
        ComponentCategory cat = ComponentCategory.fromString(category);
        if (cat == null) {
            throw new InvalidCategoryException(category);
        }
        return componentRepository.searchByNameAndCategory(searchText, cat.name()).stream()
                .map(ComponentEntity::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Find components by brand.
     */
    @Transactional(readOnly = true)
    public List<Component> findByBrand(String brand) {
        return componentRepository.findByBrand(brand).stream()
                .map(ComponentEntity::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Find components by tag.
     */
    @Transactional(readOnly = true)
    public List<Component> findByTag(String tag) {
        return componentRepository.findByTag(tag).stream()
                .map(ComponentEntity::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Find components in stock.
     */
    @Transactional(readOnly = true)
    public List<Component> findInStock() {
        return componentRepository.findByInStockTrue().stream()
                .map(ComponentEntity::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Find components by price range.
     */
    @Transactional(readOnly = true)
    public List<Component> findByPriceRange(double minPrice, double maxPrice) {
        return componentRepository.findByPriceBetween(
                BigDecimal.valueOf(minPrice),
                BigDecimal.valueOf(maxPrice)).stream()
                .map(ComponentEntity::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Find components by category and price range.
     */
    @Transactional(readOnly = true)
    public List<Component> findByCategoryAndPriceRange(String category, double minPrice, double maxPrice) {
        ComponentCategory cat = ComponentCategory.fromString(category);
        if (cat == null) {
            throw new InvalidCategoryException(category);
        }
        return componentRepository.findByCategoryAndPriceBetween(
                cat.name(),
                BigDecimal.valueOf(minPrice),
                BigDecimal.valueOf(maxPrice)).stream()
                .map(ComponentEntity::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Count components by category.
     */
    @Transactional(readOnly = true)
    public long countByCategory(String category) {
        ComponentCategory cat = ComponentCategory.fromString(category);
        if (cat == null) {
            throw new InvalidCategoryException(category);
        }
        return componentRepository.countByCategory(cat.name());
    }

    /**
     * Check if component exists.
     */
    @Transactional(readOnly = true)
    public boolean exists(String id) {
        return componentRepository.existsById(id);
    }

    /**
     * Get total count of components.
     */
    @Transactional(readOnly = true)
    public long count() {
        return componentRepository.count();
    }

    /**
     * Delete all components (use with caution!).
     */
    public void deleteAll() {
        componentRepository.deleteAll();
        log.warn("Deleted all components from database");
    }

    // ==================== HELPER METHODS ====================

    /**
     * Create a Pageable with validation.
     */
    private Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        int validPage = Math.max(0, page);
        int validSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);

        String validSortBy = sortBy != null && !sortBy.isBlank() ? sortBy : "name";
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;

        return PageRequest.of(validPage, validSize, Sort.by(direction, validSortBy));
    }
}
