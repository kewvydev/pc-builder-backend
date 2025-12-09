package com.pcBuilder.backend.repository;

import com.pcBuilder.backend.dto.ComponentListDto;
import com.pcBuilder.backend.model.component.ComponentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ComponentEntity database operations.
 * 
 * OPTIMIZATION NOTES:
 * - Use lightweight DTO projections for listings (avoid N+1 and Cartesian Product)
 * - Use paginated queries for large result sets
 * - Use EntityGraph only when attributes/tags are actually needed
 * - Avoid @EntityGraph with multiple collections (causes Cartesian Product)
 */
@Repository
public interface ComponentRepository extends JpaRepository<ComponentEntity, String>,
        JpaSpecificationExecutor<ComponentEntity> {

    // ==================== OPTIMIZED LIGHTWEIGHT QUERIES ====================
    // These use DTO projection to avoid loading attributes/tags collections

    /**
     * Find components by category with lightweight DTO projection (NO attributes/tags).
     * This is the FASTEST way to list components.
     */
    @Query("SELECT new com.pcBuilder.backend.dto.ComponentListDto(" +
           "c.id, c.category, c.name, c.brand, c.price, c.previousPrice, " +
           "c.imageUrl, c.productUrl, c.inStock, c.stockUnits) " +
           "FROM ComponentEntity c WHERE c.category = :category")
    List<ComponentListDto> findByCategoryLightweight(@Param("category") String category);

    /**
     * Find components by category with pagination (lightweight DTO).
     */
    @Query("SELECT new com.pcBuilder.backend.dto.ComponentListDto(" +
           "c.id, c.category, c.name, c.brand, c.price, c.previousPrice, " +
           "c.imageUrl, c.productUrl, c.inStock, c.stockUnits) " +
           "FROM ComponentEntity c WHERE c.category = :category")
    Page<ComponentListDto> findByCategoryLightweight(@Param("category") String category, Pageable pageable);

    /**
     * Find all components with pagination (lightweight DTO).
     */
    @Query("SELECT new com.pcBuilder.backend.dto.ComponentListDto(" +
           "c.id, c.category, c.name, c.brand, c.price, c.previousPrice, " +
           "c.imageUrl, c.productUrl, c.inStock, c.stockUnits) " +
           "FROM ComponentEntity c")
    Page<ComponentListDto> findAllLightweight(Pageable pageable);

    /**
     * Search components by name with pagination (lightweight DTO).
     */
    @Query("SELECT new com.pcBuilder.backend.dto.ComponentListDto(" +
           "c.id, c.category, c.name, c.brand, c.price, c.previousPrice, " +
           "c.imageUrl, c.productUrl, c.inStock, c.stockUnits) " +
           "FROM ComponentEntity c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    Page<ComponentListDto> searchByNameLightweight(@Param("searchText") String searchText, Pageable pageable);

    /**
     * Search components by name and category with pagination (lightweight DTO).
     */
    @Query("SELECT new com.pcBuilder.backend.dto.ComponentListDto(" +
           "c.id, c.category, c.name, c.brand, c.price, c.previousPrice, " +
           "c.imageUrl, c.productUrl, c.inStock, c.stockUnits) " +
           "FROM ComponentEntity c WHERE c.category = :category AND LOWER(c.name) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    Page<ComponentListDto> searchByNameAndCategoryLightweight(
            @Param("searchText") String searchText,
            @Param("category") String category,
            Pageable pageable);

    /**
     * Find components by category and price range with pagination (lightweight DTO).
     */
    @Query("SELECT new com.pcBuilder.backend.dto.ComponentListDto(" +
           "c.id, c.category, c.name, c.brand, c.price, c.previousPrice, " +
           "c.imageUrl, c.productUrl, c.inStock, c.stockUnits) " +
           "FROM ComponentEntity c WHERE c.category = :category AND c.price BETWEEN :minPrice AND :maxPrice")
    Page<ComponentListDto> findByCategoryAndPriceBetweenLightweight(
            @Param("category") String category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    /**
     * Find components by brand with pagination (lightweight DTO).
     */
    @Query("SELECT new com.pcBuilder.backend.dto.ComponentListDto(" +
           "c.id, c.category, c.name, c.brand, c.price, c.previousPrice, " +
           "c.imageUrl, c.productUrl, c.inStock, c.stockUnits) " +
           "FROM ComponentEntity c WHERE c.brand = :brand")
    Page<ComponentListDto> findByBrandLightweight(@Param("brand") String brand, Pageable pageable);

    /**
     * Find components in stock with pagination (lightweight DTO).
     */
    @Query("SELECT new com.pcBuilder.backend.dto.ComponentListDto(" +
           "c.id, c.category, c.name, c.brand, c.price, c.previousPrice, " +
           "c.imageUrl, c.productUrl, c.inStock, c.stockUnits) " +
           "FROM ComponentEntity c WHERE c.inStock = true")
    Page<ComponentListDto> findByInStockTrueLightweight(Pageable pageable);

    // ==================== FULL ENTITY QUERIES (with attributes/tags) ====================
    // Use these ONLY when you need the full component details

    /**
     * Find a single component by ID with all attributes and tags.
     * Uses separate fetch for attributes and tags to avoid Cartesian Product.
     */
    @Query("SELECT c FROM ComponentEntity c LEFT JOIN FETCH c.attributes WHERE c.id = :id")
    Optional<ComponentEntity> findByIdWithAttributes(@Param("id") String id);

    /**
     * Find a single component by ID with tags.
     */
    @Query("SELECT c FROM ComponentEntity c LEFT JOIN FETCH c.tags WHERE c.id = :id")
    Optional<ComponentEntity> findByIdWithTags(@Param("id") String id);

    /**
     * Find components by category with ONLY attributes (no tags).
     * Avoids Cartesian Product by not fetching both collections at once.
     */
    @Query("SELECT DISTINCT c FROM ComponentEntity c LEFT JOIN FETCH c.attributes WHERE c.category = :category")
    List<ComponentEntity> findByCategoryWithAttributes(@Param("category") String category);

    /**
     * Find components by IDs with ONLY tags.
     * Use this as a second query after findByCategoryWithAttributes to avoid Cartesian Product.
     */
    @Query("SELECT DISTINCT c FROM ComponentEntity c LEFT JOIN FETCH c.tags WHERE c.id IN :ids")
    List<ComponentEntity> findByIdsWithTags(@Param("ids") List<String> ids);

    // ==================== STANDARD QUERIES (without eager fetch) ====================
    // These return entities without eagerly loading collections

    /**
     * Find all components by category (lazy collections).
     */
    List<ComponentEntity> findByCategory(String category);

    /**
     * Find all components by category with pagination (lazy collections).
     */
    Page<ComponentEntity> findByCategory(String category, Pageable pageable);

    /**
     * Find all components by brand (lazy collections).
     */
    List<ComponentEntity> findByBrand(String brand);

    /**
     * Find components by category and in stock status.
     */
    List<ComponentEntity> findByCategoryAndInStock(String category, Boolean inStock);

    /**
     * Find components with price between min and max.
     */
    List<ComponentEntity> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Find components by category and price range.
     */
    List<ComponentEntity> findByCategoryAndPriceBetween(String category, BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Search components by name containing text (case insensitive).
     */
    @Query("SELECT c FROM ComponentEntity c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<ComponentEntity> searchByName(@Param("searchText") String searchText);

    /**
     * Search components by name and category.
     */
    @Query("SELECT c FROM ComponentEntity c WHERE c.category = :category AND LOWER(c.name) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<ComponentEntity> searchByNameAndCategory(@Param("searchText") String searchText,
            @Param("category") String category);

    /**
     * Find components by tag.
     */
    @Query("SELECT DISTINCT c FROM ComponentEntity c JOIN c.tags t WHERE LOWER(t.tag) = LOWER(:tag)")
    List<ComponentEntity> findByTag(@Param("tag") String tag);

    /**
     * Find components that are in stock.
     */
    List<ComponentEntity> findByInStockTrue();

    /**
     * Count components by category.
     */
    long countByCategory(String category);

    /**
     * Check if a component exists by ID.
     */
    boolean existsById(String id);

    // ==================== AGGREGATION QUERIES ====================

    /**
     * Get all distinct brands.
     */
    @Query("SELECT DISTINCT c.brand FROM ComponentEntity c WHERE c.brand IS NOT NULL AND c.brand <> '' ORDER BY c.brand")
    List<String> findAllDistinctBrands();

    /**
     * Get all distinct brands by category.
     */
    @Query("SELECT DISTINCT c.brand FROM ComponentEntity c WHERE c.category = :category AND c.brand IS NOT NULL AND c.brand <> '' ORDER BY c.brand")
    List<String> findDistinctBrandsByCategory(@Param("category") String category);
}
