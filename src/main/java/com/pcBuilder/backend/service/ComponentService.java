package com.pcBuilder.backend.service;

import com.pcBuilder.backend.exception.InvalidCategoryException;
import com.pcBuilder.backend.exception.ResourceNotFoundException;
import com.pcBuilder.backend.model.component.Component;
import com.pcBuilder.backend.model.component.ComponentCategory;
import com.pcBuilder.backend.model.component.ComponentEntity;
import com.pcBuilder.backend.repository.ComponentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing components using PostgreSQL database.
 */
@Service
@Transactional
public class ComponentService {

    private static final Logger log = LoggerFactory.getLogger(ComponentService.class);
    private final ComponentRepository componentRepository;

    public ComponentService(ComponentRepository componentRepository) {
        this.componentRepository = componentRepository;
    }

    /**
     * Get all components from the database.
     */
    @Transactional(readOnly = true)
    public List<Component> getAll() {
        return componentRepository.findAll().stream()
                .map(ComponentEntity::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Get components by category/type.
     */
    @Transactional(readOnly = true)
    public List<Component> getByType(String type) {
        ComponentCategory category = ComponentCategory.fromString(type);
        if (category == null) {
            throw new InvalidCategoryException(type);
        }
        return componentRepository.findByCategory(category.name()).stream()
                .map(ComponentEntity::toDomain)
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
     * Get all distinct brands.
     */
    @Transactional(readOnly = true)
    public List<String> getAllBrands() {
        return componentRepository.findAll().stream()
                .map(ComponentEntity::getBrand)
                .filter(brand -> brand != null && !brand.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
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
}
