CREATE TABLE users (
    user_id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    email              VARCHAR(255) NOT NULL,
    password           VARCHAR(255) NOT NULL,
    nickname           VARCHAR(10)  NOT NULL,
    profile_image_url  VARCHAR(500),
    created_at         DATETIME(6),
    updated_at         DATETIME(6),
    deleted_at         DATETIME(6),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE image (
    image_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_filename  VARCHAR(255) NOT NULL,
    stored_filename     VARCHAR(255) NOT NULL,
    file_path          VARCHAR(500) NOT NULL,
    file_url           VARCHAR(500) NOT NULL,
    file_size          BIGINT       NOT NULL,
    content_type       VARCHAR(100) NOT NULL,
    created_at         DATETIME(6),
    updated_at         DATETIME(6),
    deleted_at         DATETIME(6)
);

CREATE TABLE user_image (
    user_id     BIGINT NOT NULL,
    image_id    BIGINT NOT NULL,
    created_at  DATETIME(6),
    updated_at  DATETIME(6),
    deleted_at  DATETIME(6),
    PRIMARY KEY (user_id, image_id),
    CONSTRAINT fk_user_image_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_user_image_image FOREIGN KEY (image_id) REFERENCES image (image_id)
);

CREATE TABLE post (
    post_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT       NOT NULL,
    title          VARCHAR(26)  NOT NULL,
    content        TEXT         NOT NULL,
    image_url      VARCHAR(500),
    comment_count  INTEGER      NOT NULL,
    view_count     INTEGER      NOT NULL,
    created_at     DATETIME(6),
    updated_at     DATETIME(6),
    deleted_at     DATETIME(6),
    CONSTRAINT fk_post_user FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE INDEX idx_post_user_id ON post (user_id);

CREATE TABLE post_image (
    post_id     BIGINT NOT NULL,
    image_id    BIGINT NOT NULL,
    created_at  DATETIME(6),
    updated_at  DATETIME(6),
    deleted_at  DATETIME(6),
    PRIMARY KEY (post_id, image_id),
    CONSTRAINT fk_post_image_post FOREIGN KEY (post_id) REFERENCES post (post_id),
    CONSTRAINT fk_post_image_image FOREIGN KEY (image_id) REFERENCES image (image_id)
);

CREATE TABLE post_like (
    post_id     BIGINT    NOT NULL,
    user_id     BIGINT    NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    PRIMARY KEY (post_id, user_id),
    CONSTRAINT fk_post_like_post FOREIGN KEY (post_id) REFERENCES post (post_id),
    CONSTRAINT fk_post_like_user FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE TABLE comment (
    comment_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id     BIGINT NOT NULL,
    user_id     BIGINT NOT NULL,
    content     TEXT   NOT NULL,
    created_at  DATETIME(6),
    updated_at  DATETIME(6),
    deleted_at  DATETIME(6),
    CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES post (post_id),
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE INDEX idx_comment_post_id ON comment (post_id);
CREATE INDEX idx_comment_user_id ON comment (user_id);

CREATE TABLE refresh_token (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    token       VARCHAR(255) NOT NULL,
    user_id     BIGINT       NOT NULL,
    expires_at  DATETIME(6)    NOT NULL,
    CONSTRAINT uk_refresh_token_token UNIQUE (token),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE INDEX idx_refresh_token_user_id ON refresh_token (user_id);
