package com.stocat.amumal.user.usecase;

import com.stocat.amumal.image.domain.Image;
import com.stocat.amumal.image.domain.ImageSubDir;
import com.stocat.amumal.image.dto.ProfileImageUploadResponse;
import com.stocat.amumal.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class UploadProfileImageUseCase {

    private final ImageService imageService;

    public ProfileImageUploadResponse execute(MultipartFile file) {
        Image image = imageService.upload(file, ImageSubDir.PROFILE_IMAGES);
        return new ProfileImageUploadResponse(image.getFileUrl());
    }
}
