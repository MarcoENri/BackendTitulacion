-- V2: Coordinador + fecha proyecto + user_career

ALTER TABLE student
    ADD COLUMN IF NOT EXISTS coordinator_id BIGINT REFERENCES app_user(id);

ALTER TABLE student
    ADD COLUMN IF NOT EXISTS thesis_project_set_at TIMESTAMP;

CREATE TABLE IF NOT EXISTS user_career (
                                           user_id   BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    career_id BIGINT NOT NULL REFERENCES career(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, career_id)
    );

CREATE INDEX IF NOT EXISTS idx_student_coordinator ON student(coordinator_id);
CREATE INDEX IF NOT EXISTS idx_student_tutor ON student(tutor_id);
CREATE INDEX IF NOT EXISTS idx_user_career_career ON user_career(career_id);
CREATE INDEX IF NOT EXISTS idx_student_career ON student(career_id);
