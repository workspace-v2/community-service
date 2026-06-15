package com.stocat.amumal.user.service;

import com.stocat.amumal.image.domain.Image;
import com.stocat.amumal.image.repository.ImageRepository;
import com.stocat.amumal.user.domain.User;
import com.stocat.amumal.user.domain.UserImage;
import com.stocat.amumal.user.repository.UserImageRepository;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserImageMappingService {

    private final ImageRepository imageRepository;
    private final UserImageRepository userImageRepository;

    public void replace(User user, String profileImageUrl) {
        userImageRepository.deleteAllById_UserId(user.getId());

        if (profileImageUrl == null || profileImageUrl.isBlank()) {
            return;
        }

        Optional<Image> image = imageRepository.findByFileUrl(profileImageUrl.trim());
        image.ifPresent(img -> userImageRepository.save(UserImage.of(user, img)));
    }
}
