package com.pcBuilder.backend.repository;

import com.pcBuilder.backend.model.component.ComponentEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for ComponentEntity database operations.
 */
@Repository
public interface ComponentRepository extends JpaRepository<ComponentEntity, String>,
        JpaSpecificationExecutor<ComponentEntity> {

    /**
     * Fetch all components with attributes and tags to avoid N+1 queries.
     */
    @Override
    @EntityGraph(attributePaths = { "attributes", "tags" })
    List<ComponentEntity> findAll();

    /**
     * Find all components by category.
     */
    @EntityGraph(attributePaths = { "attributes", "tags" })
    List<ComponentEntity> findByCategory(String category);

    /**
     * Find all components by brand.
     */
    @EntityGraph(attributePaths = { "attributes", "tags" })
    List<ComponentEntity> findByBrand(String brand);

    /**
     * Find components by category and in stock status.
     */
    @EntityGraph(attributePaths = { "attributes", "tags" })
    List<ComponentEntity> findByCategoryAndInStock(String category, Boolean inStock);

    /**
     * Find components with price between min and max.
     */
    @EntityGraph(attributePaths = { "attributes", "tags" })
    List<ComponentEntity> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Find components by category and price range.
     */
    @EntityGraph(attributePaths = { "attributes", "tags" })
    List<ComponentEntity> findByCategoryAndPriceBetween(String category, BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Search components by name containing text (case insensitive).
     */
    @Query("SELECT c FROM ComponentEntity c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    @EntityGraph(attributePaths = { "attributes", "tags" })
    List<ComponentEntity> searchByName(@Param("searchText") String searchText);

    /**
     * Search components by name and category.
     */
    @Query("SELECT c FROM ComponentEntity c WHERE c.category = :category AND LOWER(c.name) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    @EntityGraph(attributePaths = { "attributes", "tags" })
    List<ComponentEntity> searchByNameAndCategory(@Param("searchText") String searchText,
            @Param("category") String category);

    /**
     * Find components by tag.
     */
    @Query("SELECT DISTINCT c FROM ComponentEntity c JOIN c.tags t WHERE LOWER(t.tag) = LOWER(:tag)")
    @EntityGraph(attributePaths = { "attributes", "tags" })
    List<ComponentEntity> findByTag(@Param("tag") String tag);

    /**
     * Find components that are in stock.
     */
    @EntityGraph(attributePaths = { "attributes", "tags" })
    List<ComponentEntity> findByInStockTrue();

    /**
     * Count components by category.
     */
    long countByCategory(String category);

    /**
     * Check if a component exists by ID.
     */
    boolean existsById(String id);
}
