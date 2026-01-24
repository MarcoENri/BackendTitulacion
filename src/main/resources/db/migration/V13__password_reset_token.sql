CREATE TABLE IF NOT EXISTS password_reset_token (
                                                    id BIGSERIAL PRIMARY KEY,
                                                    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES app_user(id),
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS ix_password_reset_user_id
    ON password_reset_token(user_id);
