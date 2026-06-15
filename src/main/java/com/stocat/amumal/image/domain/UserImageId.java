package com.stocat.amumal.image.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
public class UserImageId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "image_id")
    private Long imageId;

    public UserImageId(Long userId, Long imageId) {
        this.userId = userId;
        this.imageId = imageId;
    }
}
