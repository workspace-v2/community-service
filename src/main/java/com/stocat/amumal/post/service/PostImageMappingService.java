package com.stocat.amumal.post.service;

import com.stocat.amumal.image.domain.Image;
import com.stocat.amumal.image.repository.ImageRepository;
import com.stocat.amumal.post.domain.Post;
import com.stocat.amumal.post.domain.PostImage;
import com.stocat.amumal.post.repository.PostImageRepository;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PostImageMappingService {

    private final ImageRepository imageRepository;
    private final PostImageRepository postImageRepository;

    public void replace(Post post, String imageUrl) {
        postImageRepository.deleteAllById_PostId(post.getId());

        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        Optional<Image> image = imageRepository.findByFileUrl(imageUrl.trim());
        image.ifPresent(img -> postImageRepository.save(PostImage.of(post, img)));
    }
}
