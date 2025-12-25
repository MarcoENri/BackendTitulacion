-- =======================================================
-- V1__init_titulacion.sql  (PostgreSQL)
-- Base inicial completa para el sistema de titulación
-- =======================================================

-- =======================================================
-- TABLAS DE SEGURIDAD: USUARIOS / ROLES
-- =======================================================

CREATE TABLE IF NOT EXISTS role (
                                    id BIGSERIAL PRIMARY KEY,
                                    name VARCHAR(40) UNIQUE NOT NULL  -- ADMIN, COORDINATOR, TUTOR, JURY
    );

CREATE TABLE IF NOT EXISTS app_user (
                                        id BIGSERIAL PRIMARY KEY,
                                        username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS user_role (
                                         user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES role(id),
    PRIMARY KEY (user_id, role_id)
    );

-- Roles base
INSERT INTO role(name) VALUES ('ADMIN') ON CONFLICT DO NOTHING;
INSERT INTO role(name) VALUES ('COORDINATOR') ON CONFLICT DO NOTHING;
INSERT INTO role(name) VALUES ('TUTOR') ON CONFLICT DO NOTHING;
INSERT INTO role(name) VALUES ('JURY') ON CONFLICT DO NOTHING;


-- =======================================================
-- TABLAS BÁSICAS
-- =======================================================

-- CARRERAS
CREATE TABLE IF NOT EXISTS career (
                                      id BIGSERIAL PRIMARY KEY,
                                      name VARCHAR(120) UNIQUE NOT NULL
    );

-- ESTUDIANTES
-- NOTE: enums como VARCHAR para que JPA EnumType.STRING funcione fácil.
CREATE TABLE IF NOT EXISTS student (
                                       id BIGSERIAL PRIMARY KEY,

                                       dni VARCHAR(20) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(180) NOT NULL,

    corte VARCHAR(30) NOT NULL,

    -- sección: DIURNA / VESPERTINA / NOCTURNA
    section VARCHAR(30) NOT NULL,

    -- modalidad (si la usas como presencial/online/hibrida). Si no, puedes eliminarla.
    modality VARCHAR(60),

    career_id BIGINT NOT NULL REFERENCES career(id),

    -- tipo de titulación: EXAMEN / PROYECTO_TECNICO / PROYECTO_CIENTIFICO
    titulation_type VARCHAR(40) NOT NULL DEFAULT 'EXAMEN',

    -- estados del estudiante (flujo)
    status VARCHAR(30) NOT NULL DEFAULT 'EN_CURSO',  -- EN_CURSO / APTO_SUSTENTAR / NO_APTO / PROGRAMADO / EN_CORRECCION / REPROBADO
    not_apt_reason TEXT,

    -- tutor individual (si no es por grupo)
    tutor_id BIGINT REFERENCES app_user(id),

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
    );

-- Índice para filtros del admin
CREATE INDEX IF NOT EXISTS idx_student_filters
    ON student(career_id, corte, status);

-- =======================================================
-- PROYECTO DEL ESTUDIANTE (historial + fecha + quién asignó)
-- =======================================================

CREATE TABLE IF NOT EXISTS student_project (
                                               id BIGSERIAL PRIMARY KEY,
                                               student_id BIGINT NOT NULL REFERENCES student(id) ON DELETE CASCADE,

    project_name VARCHAR(255) NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT NOW(),
    assigned_by_user_id BIGINT REFERENCES app_user(id) ON DELETE SET NULL,

    is_current BOOLEAN NOT NULL DEFAULT TRUE
    );

-- Solo 1 proyecto actual por estudiante
CREATE UNIQUE INDEX IF NOT EXISTS uq_student_current_project
    ON student_project(student_id)
    WHERE is_current = TRUE;

CREATE INDEX IF NOT EXISTS idx_student_project_student
    ON student_project(student_id);


-- =======================================================
-- GRUPOS DE TUTOR (para asignar por grupo)
-- =======================================================

CREATE TABLE IF NOT EXISTS tutor_group (
                                           id BIGSERIAL PRIMARY KEY,
                                           name VARCHAR(120) NOT NULL,
    tutor_id BIGINT NOT NULL REFERENCES app_user(id),
    coordinator_id BIGINT REFERENCES app_user(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS tutor_group_student (
                                                   tutor_group_id BIGINT NOT NULL REFERENCES tutor_group(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES student(id) ON DELETE CASCADE,
    PRIMARY KEY (tutor_group_id, student_id)
    );

CREATE INDEX IF NOT EXISTS idx_group_student
    ON tutor_group_student(student_id);


-- =======================================================
-- INCIDENCIAS
-- =======================================================

CREATE TABLE IF NOT EXISTS incident (
                                        id BIGSERIAL PRIMARY KEY,
                                        student_id BIGINT NOT NULL REFERENCES student(id) ON DELETE CASCADE,

    stage VARCHAR(40) NOT NULL, -- MODALIDAD, ANTEPROYECTO, TUTORIAS, SUSTENTACIONES, GRADUACION
    date DATE NOT NULL,
    reason TEXT NOT NULL,
    action TEXT NOT NULL,

    created_by_user_id BIGINT REFERENCES app_user(id) ON DELETE SET NULL,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_incident_student
    ON incident(student_id);


-- =======================================================
-- OBSERVACIONES
-- =======================================================

CREATE TABLE IF NOT EXISTS observation (
                                           id BIGSERIAL PRIMARY KEY,
                                           student_id BIGINT NOT NULL REFERENCES student(id) ON DELETE CASCADE,

    author VARCHAR(120) NOT NULL,
    text TEXT NOT NULL,

    author_user_id BIGINT REFERENCES app_user(id) ON DELETE SET NULL,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_observation_student
    ON observation(student_id);


-- =======================================================
-- ASISTENCIA + CONFIG
-- =======================================================

CREATE TABLE IF NOT EXISTS attendance (
                                          id BIGSERIAL PRIMARY KEY,
                                          student_id BIGINT NOT NULL REFERENCES student(id) ON DELETE CASCADE,

    event_name VARCHAR(120) NOT NULL,
    event_date DATE NOT NULL,
    attended BOOLEAN NOT NULL DEFAULT TRUE,
    total_hours NUMERIC(5,2),

    created_at TIMESTAMP NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_attendance_student
    ON attendance(student_id);

CREATE TABLE IF NOT EXISTS system_config (
                                             id BIGSERIAL PRIMARY KEY,
                                             key VARCHAR(80) UNIQUE NOT NULL,
    value VARCHAR(255) NOT NULL
    );

INSERT INTO system_config (key, value)
VALUES ('MIN_ATTENDANCE_PERCENT', '75')
    ON CONFLICT (key) DO NOTHING;


-- =======================================================
-- TRIBUNALES + SUSTENTACIÓN
-- =======================================================

CREATE TABLE IF NOT EXISTS tribunal (
                                        id BIGSERIAL PRIMARY KEY,
                                        name VARCHAR(120) NOT NULL,
    members TEXT NOT NULL, -- luego si quieres lo pasamos a JSONB
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS defense_slot (
                                            id BIGSERIAL PRIMARY KEY,

                                            student_id BIGINT NOT NULL REFERENCES student(id) ON DELETE CASCADE,
    tribunal_id BIGINT NOT NULL REFERENCES tribunal(id),

    date TIMESTAMP NOT NULL,
    room VARCHAR(40),

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    UNIQUE(student_id),
    UNIQUE(date, room)
    );


-- =======================================================
-- PREDEFENSA (plazo 7 días)
-- =======================================================

CREATE TABLE IF NOT EXISTS predefense_evaluation (
                                                     id BIGSERIAL PRIMARY KEY,
                                                     student_id BIGINT NOT NULL REFERENCES student(id) ON DELETE CASCADE,

    tribunal_id BIGINT REFERENCES tribunal(id) ON DELETE SET NULL,

    result VARCHAR(30) NOT NULL, -- APROBADO / CON_OBSERVACIONES / REPROBADO
    observations TEXT,

    evaluated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    correction_deadline DATE,
    correction_submitted BOOLEAN NOT NULL DEFAULT FALSE,
    correction_submitted_at TIMESTAMP,
    correction_file_url VARCHAR(500),

    created_by_user_id BIGINT REFERENCES app_user(id) ON DELETE SET NULL
    );

CREATE UNIQUE INDEX IF NOT EXISTS uq_predefense_student
    ON predefense_evaluation(student_id);

CREATE INDEX IF NOT EXISTS idx_predefense_deadline
    ON predefense_evaluation(correction_deadline);


-- =======================================================
-- EMAIL LOG (auditoría)
-- =======================================================

CREATE TABLE IF NOT EXISTS email_log (
                                         id BIGSERIAL PRIMARY KEY,

                                         to_email VARCHAR(180) NOT NULL,
    subject VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,

    related_student_id BIGINT REFERENCES student(id) ON DELETE SET NULL,
    related_incident_id BIGINT REFERENCES incident(id) ON DELETE SET NULL,
    sent_by_user_id BIGINT REFERENCES app_user(id) ON DELETE SET NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'SENT', -- SENT / FAILED
    error_message TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT NOW()
    );


-- =======================================================
-- CARGA MASIVA (Excel/CSV)
-- =======================================================

CREATE TABLE IF NOT EXISTS student_import_batch (
                                                    id BIGSERIAL PRIMARY KEY,
                                                    uploaded_by BIGINT REFERENCES app_user(id) ON DELETE SET NULL,

    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(20) NOT NULL, -- XLSX / CSV

    total_rows INT NOT NULL DEFAULT 0,
    inserted_rows INT NOT NULL DEFAULT 0,
    updated_rows INT NOT NULL DEFAULT 0,
    failed_rows INT NOT NULL DEFAULT 0,

    status VARCHAR(30) NOT NULL DEFAULT 'PROCESSING', -- PROCESSING / COMPLETED / FAILED
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS student_import_row (
                                                  id BIGSERIAL PRIMARY KEY,
                                                  batch_id BIGINT NOT NULL REFERENCES student_import_batch(id) ON DELETE CASCADE,
    row_number INT NOT NULL,

    dni VARCHAR(20),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(180),
    corte VARCHAR(30),
    section VARCHAR(30),
    modality VARCHAR(60),
    career_name VARCHAR(120),

    status VARCHAR(20) NOT NULL DEFAULT 'OK', -- OK / ERROR
    error_message TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_import_batch
    ON student_import_row(batch_id);
