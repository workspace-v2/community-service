package com.stocat.amumal.image.service;

import com.stocat.amumal.image.dto.PostFileUploadResponse;
import com.stocat.amumal.image.dto.ProfileImageUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    PostFileUploadResponse uploadPostImage(MultipartFile file);
    ProfileImageUploadResponse uploadProfileImage(MultipartFile file);
}
