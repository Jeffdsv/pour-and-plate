-- a better rule for which kind each place is, now also using overtures hierarchy and the name
-- a generated column cannot be changed, so drop it and create it again, the index goes with it
ALTER TABLE place DROP COLUMN kind;

-- the first WHEN that matches wins
-- \m and \M mean start and end of a word, so vin matches "Mat & Vin" but not "Vinterträdgården"
-- ~* is a regex match that ignores upper and lower case
ALTER TABLE place ADD COLUMN kind VARCHAR(20) GENERATED ALWAYS AS (
    CASE
        -- what overture says about wine and beer, in the category or anywhere in the chain
        WHEN category IN ('wine_bar', 'winery', 'champagne_bar')
            OR hierarchy LIKE '%wine_bar%' OR hierarchy LIKE '%winery%' THEN 'WINE'
        WHEN category IN ('pub', 'brewery', 'brewpub', 'gastropub', 'beer_bar', 'beer_garden')
            OR hierarchy LIKE '%beer_bar%' OR hierarchy LIKE '%brewery%' OR hierarchy LIKE '%pub' THEN 'BEER'

        -- cafes stay cafes, even if the name has a word like vin in it
        WHEN category IN ('cafe', 'coffee_shop', 'smoothie_juice_bar') THEN 'CAFE'

        -- the name tells us what overture missed
        WHEN name ~* '\m(vinbar\w*|vinkällar\w*|vinstug\w*|vinotek\w*|vinoteca|enoteca|wine|vin)\M' THEN 'WINE'
        WHEN name ~* '\m(ölbar\w*|ölhall\w*|ölstug\w*|ölkällar\w*|bryggeri\w*|brewery|brewing|taproom|pub|beer|bier\w*)\M' THEN 'BEER'

        WHEN category LIKE '%restaurant'
            OR category IN ('bistro', 'tapas_bar', 'salad_bar') THEN 'RESTAURANT'
        ELSE 'BAR'
        END
    ) STORED;

CREATE INDEX idx_place_kind ON place (kind);