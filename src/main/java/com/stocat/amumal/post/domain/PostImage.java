package com.stocat.amumal.post.domain;

import com.stocat.amumal.common.entity.BaseEntity;
import com.stocat.amumal.image.domain.Image;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostImage extends BaseEntity {

    @EmbeddedId
    private PostImageId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("imageId")
    @JoinColumn(name = "image_id")
    private Image image;

    public static PostImage of(Post post, Image image) {
        PostImage postImage = new PostImage();
        postImage.id = new PostImageId(post.getId(), image.getId());
        postImage.post = post;
        postImage.image = image;
        return postImage;
    }
}
