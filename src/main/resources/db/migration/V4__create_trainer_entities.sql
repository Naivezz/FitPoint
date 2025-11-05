CREATE TABLE IF NOT EXISTS trainer_notes (
    id            BIGSERIAL PRIMARY KEY,
    trainer_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    client_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    note          TEXT NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE
    );

CREATE TABLE IF NOT EXISTS schedule_change_requests (
    id                     BIGSERIAL PRIMARY KEY,
    trainer_id             BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    training_class_id      BIGINT REFERENCES training_classes(id) ON DELETE SET NULL,
    request_type           VARCHAR(16) NOT NULL,
    reason                 TEXT,
    class_name             VARCHAR(255),
    class_description      TEXT,
    requested_start_time   TIMESTAMP WITHOUT TIME ZONE,
    requested_end_time     TIMESTAMP WITHOUT TIME ZONE,
    requested_capacity     INTEGER,
    requested_room_id      BIGINT REFERENCES rooms(id) ON DELETE SET NULL,
    status                 VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    created_at             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    reviewed_at            TIMESTAMP WITHOUT TIME ZONE,
    reviewed_by            BIGINT REFERENCES users(id),
    review_note            TEXT
    );

CREATE TABLE IF NOT EXISTS personal_training_sessions (
    id             BIGSERIAL PRIMARY KEY,
    trainer_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    client_id      BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    start_time     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_time       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    session_goal   VARCHAR(500),
    session_notes  TEXT,
    status         VARCHAR(16) NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITHOUT TIME ZONE
);
