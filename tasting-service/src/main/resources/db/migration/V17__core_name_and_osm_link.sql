-- the core of a name, without words that many places have, used to compare names
-- "Restaurang Hemma Vasastan" becomes "hemma vasastan"
-- IMMUTABLE means the same name always gives the same answer, so postgres may use it in indexes
CREATE FUNCTION core_name(name TEXT) RETURNS TEXT
    LANGUAGE sql IMMUTABLE AS $$
SELECT trim(regexp_replace(
        regexp_replace(lower(name), '\m(restaurang|restaurant|pizzeria|pizza|café|cafe|kafé|bar|bistro|grill|och|and|ab|nya|the)\M', ' ', 'g'),
        '[^a-z0-9åäöéü ]', ' ', 'g'))
           $$;

-- the matching openstreetmap place, set when both sources have the same place, null when only overture has it
ALTER TABLE place ADD COLUMN osm_id VARCHAR(64);