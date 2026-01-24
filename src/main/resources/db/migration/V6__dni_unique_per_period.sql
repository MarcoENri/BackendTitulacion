-- 1) quitar unique viejo (nombre común: student_dni_key)
ALTER TABLE student DROP CONSTRAINT IF EXISTS student_dni_key;

-- 2) crear unique compuesto por periodo
ALTER TABLE student
    ADD CONSTRAINT uq_student_dni_period UNIQUE (dni, academic_period_id);
