-- kinds follow what a place is focused on: a pub or wine bar is about drinks, a pizzeria is about food
-- v14 has already run, so the improved rule goes in a new migration instead of changing v14
ALTER TABLE place DROP COLUMN kind;

ALTER TABLE place ADD COLUMN kind VARCHAR(20) GENERATED ALWAYS AS (
    CASE
    -- what overture says about wine and beer, in the category or anywhere in the chain
    WHEN category IN ('wine_bar', 'winery', 'champagne_bar')
    OR hierarchy LIKE '%wine_bar%' OR hierarchy LIKE '%winery%' THEN 'WINE'
    WHEN category IN ('pub', 'brewery', 'brewpub', 'gastropub', 'beer_bar', 'beer_garden')
    OR hierarchy LIKE '%beer_bar%' OR hierarchy LIKE '%brewery%' OR hierarchy LIKE '%pub' THEN 'BEER'

    -- cafes stay cafes, pizzerias and burger places stay restaurants, their focus is food
    WHEN category IN ('cafe', 'coffee_shop', 'smoothie_juice_bar') THEN 'CAFE'
    WHEN category IN ('pizza_restaurant', 'burger_restaurant') THEN 'RESTAURANT'

    -- strong wine words in the name mean a wine place, whatever overture called it
    WHEN name ~* '\m(vinbar\w*|vinkällar\w*|vinotek\w*|vinoteca|champagnebar\w*)\M'
    OR name ~* 'wine[ -]?bar' THEN 'WINE'

        -- weak wine words like "mat & vin" only count when overture does not already call it a restaurant
        WHEN name ~* '\m(wine|vin|enoteca|vinstug\w*)\M'
          AND category NOT LIKE '%restaurant'
          AND category NOT IN ('bistro', 'tapas_bar', 'salad_bar') THEN 'WINE'

        -- beer words in the name, pizzerias and burger places are already handled above
        WHEN name ~* '\m(ölbar\w*|ölhall\w*|ölstug\w*|ölkällar\w*|bryggeri\w*|brewery|brewing|taproom|pub|beer|bier\w*)\M' THEN 'BEER'

        WHEN category LIKE '%restaurant'
          OR category IN ('bistro', 'tapas_bar', 'salad_bar') THEN 'RESTAURANT'
        ELSE 'BAR'
END
) STORED;

CREATE INDEX idx_place_kind ON place (kind);