-- Migration script to add performance indexes
-- Run this script manually if the indexes don't exist

-- Index for in_stock column
CREATE INDEX IF NOT EXISTS idx_components_in_stock
    ON components (in_stock);

-- Composite index for category + price (optimizes category filtering with price sorting)
CREATE INDEX IF NOT EXISTS idx_components_category_price
    ON components (category, price);

-- Composite index for category + in_stock
CREATE INDEX IF NOT EXISTS idx_components_category_instock
    ON components (category, in_stock);

-- Index for case-insensitive name search
CREATE INDEX IF NOT EXISTS idx_components_name_lower
    ON components (LOWER(name));

-- Verify indexes were created
SELECT indexname, indexdef 
FROM pg_indexes 
WHERE tablename = 'components'
ORDER BY indexname;

