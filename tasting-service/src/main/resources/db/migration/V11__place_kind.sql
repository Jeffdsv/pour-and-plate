-- every place gets a kind for the filter on the map, worked out from its category
-- GENERATED ALWAYS means postgres fills it in by itself, also for places the import adds later
-- the order matters: the first matching WHEN wins
ALTER TABLE place ADD COLUMN kind VARCHAR(20) GENERATED ALWAYS AS (
    CASE
        WHEN category IN ('cafe', 'coffee_shop', 'smoothie_juice_bar') THEN 'CAFE'
        WHEN category IN ('wine_bar', 'winery', 'champagne_bar') THEN 'WINE'
        WHEN category IN ('pub', 'brewery', 'brewpub', 'gastropub', 'beer_bar') THEN 'BEER'
        WHEN category LIKE '%restaurant'
            OR category IN ('bistro', 'tapas_bar', 'salad_bar') THEN 'RESTAURANT'
        ELSE 'BAR'
        END
    ) STORED;

-- the map asks for places of certain kinds, so make that lookup fast
CREATE INDEX idx_place_kind ON place (kind);