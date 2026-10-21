-- saved restaurants remember the website too, so the popup can show it
ALTER TABLE restaurant ADD COLUMN website VARCHAR(1000);

-- fill in the website for places that were saved before this column existed
-- by copying it from the matching row in the place table
UPDATE restaurant r
SET website = p.website
    FROM place p
WHERE p.id = r.place_id;