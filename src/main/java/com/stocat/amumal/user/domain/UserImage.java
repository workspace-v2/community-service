package com.stocat.amumal.user.domain;

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
@Table(name = "user_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserImage extends BaseEntity {

    @EmbeddedId
    private UserImageId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("imageId")
    @JoinColumn(name = "image_id")
    private Image image;

    public static UserImage of(User user, Image image) {
        UserImage userImage = new UserImage();
        userImage.id = new UserImageId(user.getId(), image.getId());
        userImage.user = user;
        userImage.image = image;
        return userImage;
    }
}
