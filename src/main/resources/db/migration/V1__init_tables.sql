CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    phone      VARCHAR(20)
);

CREATE TABLE roles
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE user_roles
(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

CREATE TABLE rooms
(
    id       BIGSERIAL PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    capacity INTEGER      NOT NULL
);

CREATE TABLE equipment
(
    id       BIGSERIAL PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    quantity INTEGER      NOT NULL,
    room_id  BIGINT,
    status   VARCHAR(50),
    FOREIGN KEY (room_id) REFERENCES rooms (id) ON DELETE SET NULL
);

CREATE TABLE training_classes
(
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    description    TEXT,
    trainer_id     BIGINT,
    room_id        BIGINT,
    start_time     TIMESTAMP    NOT NULL,
    end_time       TIMESTAMP    NOT NULL,
    capacity       INTEGER      NOT NULL,
    average_rating DOUBLE PRECISION,
    FOREIGN KEY (trainer_id) REFERENCES users (id) ON DELETE SET NULL,
    FOREIGN KEY (room_id) REFERENCES rooms (id) ON DELETE SET NULL
);

CREATE TABLE memberships
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT         NOT NULL,
    type       VARCHAR(50)    NOT NULL,
    start_date DATE           NOT NULL,
    end_date   DATE           NOT NULL,
    price      DECIMAL(10, 2) NOT NULL,
    active     BOOLEAN        NOT NULL DEFAULT true,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE reservations
(
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT      NOT NULL,
    training_class_id BIGINT      NOT NULL,
    reservation_date  TIMESTAMP   NOT NULL,
    status            VARCHAR(50) NOT NULL,
    rating            INTEGER CHECK (rating >= 1 AND rating <= 5),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (training_class_id) REFERENCES training_classes (id) ON DELETE CASCADE
);

CREATE TABLE promotions
(
    id               BIGSERIAL PRIMARY KEY,
    title            VARCHAR(255)  NOT NULL,
    description      TEXT,
    discount_percent DECIMAL(5, 2) NOT NULL,
    start_date       DATE          NOT NULL,
    end_date         DATE          NOT NULL
);

CREATE TABLE notifications
(
    id           BIGSERIAL PRIMARY KEY,
    recipient_id BIGINT    NOT NULL,
    message      TEXT      NOT NULL,
    sent_at      TIMESTAMP NOT NULL,
    read         BOOLEAN   NOT NULL DEFAULT false,
    FOREIGN KEY (recipient_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE coupons
(
    id             BIGSERIAL PRIMARY KEY,
    code           VARCHAR(50)    NOT NULL UNIQUE,
    discount_value DECIMAL(10, 2) NOT NULL,
    active         BOOLEAN        NOT NULL DEFAULT true,
    used_by_id     BIGINT,
    expires_at     TIMESTAMP      NOT NULL,
    FOREIGN KEY (used_by_id) REFERENCES users (id) ON DELETE SET NULL
);


INSERT INTO roles (name)
VALUES ('ROLE_CLIENT'),
       ('ROLE_TRAINER'),
       ('ROLE_ADMIN');