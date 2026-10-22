-- pg_trgm splits text into groups of three letters, which makes "contains" searches fast
-- on azure it must first be allowed with the azure.extensions server setting
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- a gin index on those letter groups, so name ILIKE '%ekst%' does not have to read every row
CREATE INDEX idx_place_name_trgm ON place USING gin (name gin_trgm_ops);