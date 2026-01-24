-- V11__career_add_normalized_name.sql

CREATE EXTENSION IF NOT EXISTS unaccent;

ALTER TABLE career
    ADD COLUMN IF NOT EXISTS normalized_name varchar(255);

UPDATE career
SET normalized_name = lower(regexp_replace(unaccent(trim(name)), '\s+', ' ', 'g'))
WHERE normalized_name IS NULL OR normalized_name = '';
