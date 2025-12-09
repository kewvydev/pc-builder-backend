# Performance Optimization Summary

## Problem
Query for ~1000 CPUs was taking **>1 minute** instead of milliseconds, even though PostgreSQL EXPLAIN ANALYZE showed a fast sequential scan (~3ms).

## Root Causes Identified

### 1. **Cartesian Product from @EntityGraph** (MAIN ISSUE)
The `@EntityGraph(attributePaths = { "attributes", "tags" })` was causing a Cartesian Product. 

With 1000 components × 20 attributes × 5 tags = **100,000 rows** returned from the database, which Hibernate then had to process to build just 1000 objects.

### 2. **Bidirectional Relationships Without @JsonIgnore**
`ComponentAttributeEntity` and `ComponentTagEntity` had `@ManyToOne` back to `ComponentEntity` without `@JsonIgnore`, causing potential infinite recursion during JSON serialization.

### 3. **Lombok's @Data Triggering Lazy Loading**
`@Data` generates `toString()`, `hashCode()`, and `equals()` that accessed lazy collections, potentially triggering unwanted database queries.

### 4. **getAllBrands() Loading All Entities**
The method was loading all components just to extract distinct brands, instead of using a dedicated query.

---

## Solutions Implemented

### 1. Created Lightweight DTOs

**`ComponentListDto.java`** - For listings without attributes/tags:
```java
- id, category, name, brand, price, previousPrice
- imageUrl, productUrl, inStock, stockUnits
- NO attributes, NO tags
```

**`PagedResponse.java`** - Generic paginated response wrapper

### 2. Optimized Repository Queries

**Lightweight DTO Projections:**
```java
@Query("SELECT new com.pcBuilder.backend.dto.ComponentListDto(...) 
        FROM ComponentEntity c WHERE c.category = :category")
List<ComponentListDto> findByCategoryLightweight(@Param("category") String category);

Page<ComponentListDto> findByCategoryLightweight(@Param("category") String category, Pageable pageable);
```

**Two-Query Approach (avoids Cartesian Product):**
```java
// Query 1: Get components with attributes
@Query("SELECT DISTINCT c FROM ComponentEntity c LEFT JOIN FETCH c.attributes WHERE c.category = :category")
List<ComponentEntity> findByCategoryWithAttributes(@Param("category") String category);

// Query 2: Get tags separately
@Query("SELECT DISTINCT c FROM ComponentEntity c LEFT JOIN FETCH c.tags WHERE c.id IN :ids")
List<ComponentEntity> findByIdsWithTags(@Param("ids") List<String> ids);
```

**Optimized Aggregation Queries:**
```java
@Query("SELECT DISTINCT c.brand FROM ComponentEntity c WHERE c.brand IS NOT NULL ORDER BY c.brand")
List<String> findAllDistinctBrands();
```

### 3. Fixed Entity Annotations

**ComponentEntity.java:**
```java
@OneToMany(...)
@ToString.Exclude
@EqualsAndHashCode.Exclude
private List<ComponentAttributeEntity> attributes;

@OneToMany(...)
@ToString.Exclude
@EqualsAndHashCode.Exclude
private Set<ComponentTagEntity> tags;
```

**ComponentAttributeEntity.java & ComponentTagEntity.java:**
```java
@ManyToOne(...)
@JsonIgnore
@ToString.Exclude
@EqualsAndHashCode.Exclude
private ComponentEntity component;
```

### 4. Added Pagination Support

All listing endpoints now support pagination:
- `Page<ComponentListDto>` return types
- `Pageable` parameters
- Max page size limit (200)

### 5. Hibernate Performance Configuration

```properties
spring.jpa.properties.hibernate.default_batch_fetch_size=50
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```

### 6. Additional Database Indexes

```sql
CREATE INDEX idx_components_category_price ON components (category, price);
CREATE INDEX idx_components_category_instock ON components (category, in_stock);
CREATE INDEX idx_components_name_lower ON components (LOWER(name));
CREATE INDEX idx_components_in_stock ON components (in_stock);
```

---

## New API Endpoints (v2 - RECOMMENDED)

### Optimized Paginated Endpoints

| Endpoint | Description |
|----------|-------------|
| `GET /api/components/v2/{category}?page=0&size=50` | Paginated lightweight list |
| `GET /api/components/v2/{category}/all` | All items (lightweight DTO) |
| `GET /api/components/v2?page=0&size=50` | All categories paginated |
| `GET /api/components/v2/search?query=...&page=0` | Search with pagination |
| `GET /api/components/v2/search/{category}?query=...` | Search in category |
| `GET /api/components/v2/{category}/filter?minPrice=...&maxPrice=...` | Price filter |
| `GET /api/components/v2/detail/{id}` | Full details (with attributes/tags) |
| `GET /api/components/v2/{category}/brands` | Brands in category |

### Query Parameters

- `page`: Page number (0-based, default: 0)
- `size`: Items per page (1-200, default: 50)
- `sortBy`: Field to sort by (default: "name")
- `sortDir`: "asc" or "desc" (default: "asc")

### Response Format

```json
{
  "content": [...],
  "page": 0,
  "size": 50,
  "totalElements": 1000,
  "totalPages": 20,
  "first": true,
  "last": false,
  "hasNext": true,
  "hasPrevious": false
}
```

---

## Legacy Endpoints (Backward Compatible)

The original endpoints still work but now use optimized queries internally:

| Endpoint | Note |
|----------|------|
| `GET /api/components/{type}` | Now uses two-query approach |
| `GET /api/components/{type}/{id}` | Works as before |

---

## Expected Performance Improvement

| Metric | Before | After |
|--------|--------|-------|
| Time for 1000 CPUs | >60 seconds | <200ms |
| Queries executed | 1 (with huge result set) | 2 (optimized) |
| Result set size | ~100,000 rows | ~1000 + ~5000 rows |
| JSON payload | Full entities | Lightweight DTOs |

---

## Migration Steps

1. Run `migration-add-indexes.sql` to create new indexes
2. Deploy the updated backend
3. (Optional) Update frontend to use v2 endpoints for better UX

---

## Files Modified

- `ComponentEntity.java` - Added exclude annotations
- `ComponentAttributeEntity.java` - Added @JsonIgnore
- `ComponentTagEntity.java` - Added @JsonIgnore
- `ComponentRepository.java` - Added optimized queries
- `ComponentService.java` - Added lightweight methods
- `ComponentController.java` - Added v2 endpoints
- `application.properties` - Added Hibernate optimizations
- `schema.sql` - Added new indexes

## Files Created

- `ComponentListDto.java` - Lightweight DTO
- `PagedResponse.java` - Pagination wrapper
- `migration-add-indexes.sql` - Index migration script
- `PERFORMANCE_OPTIMIZATION.md` - This documentation

