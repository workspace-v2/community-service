ALTER TABLE post
    ADD COLUMN like_count INTEGER NOT NULL DEFAULT 0;

UPDATE post
SET like_count = (
    SELECT COUNT(*)
    FROM post_like
    WHERE post_like.post_id = post.post_id
);
