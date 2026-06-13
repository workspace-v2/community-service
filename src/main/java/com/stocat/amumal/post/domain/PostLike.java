package com.stocat.amumal.post.domain;

import com.stocat.amumal.common.entity.CreatedEntity;
import com.stocat.amumal.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "post_like")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PostLike extends CreatedEntity {

    // @EmbeddedId: 복합키 객체를 PK로 사용
    @EmbeddedId
    private PostLikeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId") // PostLikeId.postId 값을 post.id로 자동으로 채워줌
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static PostLike of(Post post, User user) {
        PostLike postLike = new PostLike();
        postLike.id = new PostLikeId(post.getId(), user.getId());
        postLike.post = post;
        postLike.user = user;
        return postLike;
    }
}
