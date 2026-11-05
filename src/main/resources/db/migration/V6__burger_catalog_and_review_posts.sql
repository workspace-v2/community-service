-- Do not fabricate menu/rating for legacy stock posts: preserve them with null menu_id.
CREATE TABLE brand (
 brand_id BIGINT AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(100) NOT NULL,
 created_at DATETIME(6), updated_at DATETIME(6), deleted_at DATETIME(6)
);
CREATE TABLE menu (
 menu_id BIGINT AUTO_INCREMENT PRIMARY KEY,
 brand_id BIGINT NOT NULL,
 name VARCHAR(150) NOT NULL,
 description TEXT NOT NULL,
 price INTEGER,
 source_type VARCHAR(20) NOT NULL,
 source_url VARCHAR(1000),
 created_at DATETIME(6), updated_at DATETIME(6), deleted_at DATETIME(6),
 CONSTRAINT fk_menu_brand FOREIGN KEY (brand_id) REFERENCES brand(brand_id),
 CONSTRAINT ck_menu_price CHECK (price IS NULL OR price >= 0),
 CONSTRAINT ck_menu_source CHECK (source_type IN ('REAL','SYNTHETIC')),
 CONSTRAINT ck_menu_real_source CHECK (source_type <> 'REAL' OR (source_url IS NOT NULL AND LENGTH(TRIM(source_url)) > 0))
);
ALTER TABLE post ADD COLUMN menu_id BIGINT;
ALTER TABLE post ADD COLUMN rating INTEGER;
ALTER TABLE post ADD CONSTRAINT fk_post_menu FOREIGN KEY(menu_id) REFERENCES menu(menu_id);
ALTER TABLE post ADD CONSTRAINT ck_post_review CHECK ((menu_id IS NULL AND rating IS NULL) OR (menu_id IS NOT NULL AND rating IS NOT NULL AND rating BETWEEN 1 AND 5));
CREATE TABLE post_tag (
 post_id BIGINT NOT NULL,
 tag VARCHAR(30) NOT NULL,
 PRIMARY KEY(post_id,tag),
 CONSTRAINT fk_post_tag_post FOREIGN KEY(post_id) REFERENCES post(post_id) ON DELETE CASCADE
);
