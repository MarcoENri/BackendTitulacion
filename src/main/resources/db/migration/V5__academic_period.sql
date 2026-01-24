CREATE TABLE IF NOT EXISTS academic_period (
                                               id BIGSERIAL PRIMARY KEY,
                                               name VARCHAR(80) NOT NULL,         -- Ej: "2025-09 / 2026-02"
    start_date DATE NOT NULL,          -- 2025-09-01
    end_date DATE NOT NULL,            -- 2026-02-28
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
    );

-- Agregar FK a student
ALTER TABLE student
    ADD COLUMN IF NOT EXISTS academic_period_id BIGINT;

ALTER TABLE student
    ADD CONSTRAINT fk_student_academic_period
        FOREIGN KEY (academic_period_id) REFERENCES academic_period(id);

CREATE INDEX IF NOT EXISTS idx_student_academic_period_id
    ON student(academic_period_id);
