-- places that were probably replaced by another place at the same address
-- they are hidden on the map but still found in search, nothing is deleted
-- scripts/mark_replaced.sql sets this, and running it again works it out from scratch
ALTER TABLE place ADD COLUMN likely_closed BOOLEAN NOT NULL DEFAULT false;