package com.stocat.amumal.post.usecase;

import com.stocat.amumal.image.domain.Image;
import com.stocat.amumal.image.domain.ImageSubDir;
import com.stocat.amumal.image.dto.PostFileUploadResponse;
import com.stocat.amumal.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class UploadPostImageUseCase {

    private final ImageService imageService;

    public PostFileUploadResponse execute(MultipartFile file) {
        Image image = imageService.upload(file, ImageSubDir.POST_IMAGES);
        return new PostFileUploadResponse(image.getFileUrl());
    }
}
