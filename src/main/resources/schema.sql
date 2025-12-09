CREATE EXTENSION IF NOT EXISTS "pgcrypto"^^

CREATE TABLE IF NOT EXISTS components (
    id TEXT PRIMARY KEY,
    category VARCHAR(50) NOT NULL,
    name TEXT NOT NULL,
    brand TEXT,
    price NUMERIC(12,2),
    previous_price NUMERIC(12,2),
    image_url TEXT,
    product_url TEXT,
    in_stock BOOLEAN NOT NULL DEFAULT TRUE,
    stock_units INTEGER NOT NULL DEFAULT 0,
    last_updated TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
)^^

CREATE TABLE IF NOT EXISTS component_attributes (
    component_id TEXT NOT NULL REFERENCES components(id) ON DELETE CASCADE,
    attribute_key TEXT NOT NULL,
    attribute_value TEXT,
    PRIMARY KEY (component_id, attribute_key)
)^^

CREATE TABLE IF NOT EXISTS component_tags (
    component_id TEXT NOT NULL REFERENCES components(id) ON DELETE CASCADE,
    tag TEXT NOT NULL,
    normalized_tag TEXT GENERATED ALWAYS AS (lower(tag)) STORED,
    PRIMARY KEY (component_id, normalized_tag)
)^^

CREATE TABLE IF NOT EXISTS builds (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    budget NUMERIC(12,2),
    estimated_power_draw NUMERIC(10,2),
    total_price NUMERIC(12,2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
)^^

CREATE TABLE IF NOT EXISTS build_components (
    build_id UUID NOT NULL REFERENCES builds(id) ON DELETE CASCADE,
    category VARCHAR(50) NOT NULL,
    component_id TEXT REFERENCES components(id),
    PRIMARY KEY (build_id, category)
)^^

CREATE TABLE IF NOT EXISTS build_alerts (
    id BIGSERIAL PRIMARY KEY,
    build_id UUID NOT NULL REFERENCES builds(id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
)^^

CREATE TABLE IF NOT EXISTS build_recommendations (
    id BIGSERIAL PRIMARY KEY,
    build_id UUID NOT NULL REFERENCES builds(id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
)^^

CREATE TABLE IF NOT EXISTS scraping_logs (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    category VARCHAR(50),
    status VARCHAR(32),
    duration_ms INTEGER,
    items_found INTEGER,
    message TEXT
)^^

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql^^

CREATE TRIGGER trg_components_updated_at
BEFORE UPDATE ON components
FOR EACH ROW
EXECUTE FUNCTION set_updated_at()^^

CREATE TRIGGER trg_builds_updated_at
BEFORE UPDATE ON builds
FOR EACH ROW
EXECUTE FUNCTION set_updated_at()^^

CREATE INDEX IF NOT EXISTS idx_components_category
    ON components (category)^^

CREATE INDEX IF NOT EXISTS idx_components_brand
    ON components (brand)^^

CREATE INDEX IF NOT EXISTS idx_components_price
    ON components (price)^^

CREATE INDEX IF NOT EXISTS idx_components_in_stock
    ON components (in_stock)^^

-- Composite index for common queries: category + price (useful for filtering by category and sorting/filtering by price)
CREATE INDEX IF NOT EXISTS idx_components_category_price
    ON components (category, price)^^

-- Composite index for category + in_stock (useful for filtering available products in a category)
CREATE INDEX IF NOT EXISTS idx_components_category_instock
    ON components (category, in_stock)^^

-- Index for name search (useful for LIKE queries)
CREATE INDEX IF NOT EXISTS idx_components_name_lower
    ON components (LOWER(name))^^

CREATE INDEX IF NOT EXISTS idx_component_attributes_key
    ON component_attributes (attribute_key)^^

CREATE INDEX IF NOT EXISTS idx_component_tags_tag
    ON component_tags (normalized_tag)^^

CREATE INDEX IF NOT EXISTS idx_build_components_component_id
    ON build_components (component_id)^^

CREATE INDEX IF NOT EXISTS idx_scraping_logs_category
    ON scraping_logs (category)^^

