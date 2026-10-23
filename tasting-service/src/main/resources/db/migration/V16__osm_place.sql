-- food and drink places from openstreetmap, kept apart until they are matched with overture
-- the id looks like osm:node/123 or osm:way/456, so it never clashes with overtures ids
CREATE TABLE osm_place (
                           id          VARCHAR(64)   PRIMARY KEY,
                           name        VARCHAR(300)  NOT NULL,
                           amenity     VARCHAR(30)   NOT NULL,
                           cuisine     VARCHAR(200),
                           address     VARCHAR(300),
                           city        VARCHAR(100),
                           latitude    NUMERIC(9, 6) NOT NULL,
                           longitude   NUMERIC(9, 6) NOT NULL,
                           website     VARCHAR(500),
                           phone       VARCHAR(50),
                           imported_at TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- the matching looks for osm places close to each overture place
CREATE INDEX idx_osm_place_location ON osm_place (latitude, longitude);