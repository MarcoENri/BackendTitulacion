-- 1) Asegura que todos tengan valor
UPDATE career
SET normalized_name = lower(regexp_replace(unaccent(trim(name)), '\s+', ' ', 'g'))
WHERE normalized_name IS NULL OR normalized_name = '';

-- 2) Ahora sí: NOT NULL
ALTER TABLE career
    ALTER COLUMN normalized_name SET NOT NULL;

-- 3) Índice UNIQUE (si ya existe, no falla)
CREATE UNIQUE INDEX IF NOT EXISTS ux_career_normalized_name
    ON career(normalized_name);
