-- V3: all food and drink places in sweden, imported from overture maps
-- only the import fills this table, my own reviews stay in the restaurant table

CREATE TABLE place (
                       id           VARCHAR(64) PRIMARY KEY,   -- the overture id, stays the same between imports
                       name         VARCHAR(300) NOT NULL,
                       category     VARCHAR(100) NOT NULL,     -- like sushi_restaurant, cafe or wine_bar
                       address      VARCHAR(300),
                       city         VARCHAR(100),
                       latitude     NUMERIC(9, 6) NOT NULL,
                       longitude    NUMERIC(9, 6) NOT NULL,
                       website      VARCHAR(500),
                       phone        VARCHAR(50),
                       imported_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- makes finding all places inside the visible map area fast
CREATE INDEX idx_place_location ON place (latitude, longitude);