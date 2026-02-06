-- ============================================
-- V14: Evaluación individual por estudiante
-- ULTRA SAFE VERSION
-- ============================================

-- --------------------------------------------
-- 0. Eliminar constraints existentes
-- --------------------------------------------

ALTER TABLE final_defense_evaluation
DROP CONSTRAINT IF EXISTS fk_fde_student;

ALTER TABLE final_defense_evaluation
DROP CONSTRAINT IF EXISTS uq_fde_booking_jury_student;

ALTER TABLE final_defense_evaluation
DROP CONSTRAINT IF EXISTS final_defense_evaluation_booking_id_jury_user_id_key;

ALTER TABLE final_defense_evaluation
DROP CONSTRAINT IF EXISTS uq_final_defense_evaluation_booking_jury;

-- --------------------------------------------
-- 1. Agregar columna student_id
-- --------------------------------------------

ALTER TABLE final_defense_evaluation
    ADD COLUMN IF NOT EXISTS student_id BIGINT;

-- --------------------------------------------
-- 2. Migrar datos antiguos
-- --------------------------------------------

UPDATE final_defense_evaluation e
SET student_id = sub.student_id
    FROM (
    SELECT DISTINCT ON (b.id)
        b.id AS booking_id,
        gm.student_id
    FROM final_defense_booking b
    JOIN final_defense_group g ON g.id = b.group_id
    JOIN final_defense_group_member gm ON gm.group_id = g.id
    ORDER BY b.id, gm.student_id
) sub
WHERE e.booking_id = sub.booking_id
  AND e.student_id IS NULL;

-- --------------------------------------------
-- 3. Hacer NOT NULL
-- --------------------------------------------

ALTER TABLE final_defense_evaluation
    ALTER COLUMN student_id SET NOT NULL;

-- --------------------------------------------
-- 4. Crear FK
-- --------------------------------------------

ALTER TABLE final_defense_evaluation
    ADD CONSTRAINT fk_fde_student
        FOREIGN KEY (student_id)
            REFERENCES student(id)
            ON DELETE CASCADE;

-- --------------------------------------------
-- 5. Crear UNIQUE correcta
-- --------------------------------------------

ALTER TABLE final_defense_evaluation
    ADD CONSTRAINT uq_fde_booking_jury_student
        UNIQUE (booking_id, jury_user_id, student_id);
