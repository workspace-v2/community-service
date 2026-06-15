package com.stocat.amumal.image.service;

import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.image.domain.Image;
import com.stocat.amumal.image.domain.ImageSubDir;
import com.stocat.amumal.image.dto.PostFileUploadResponse;
import com.stocat.amumal.image.dto.ProfileImageUploadResponse;
import com.stocat.amumal.image.repository.ImageRepository;
import com.stocat.amumal.image.storage.FileStorage;
import com.stocat.amumal.image.storage.StoredFileInfo;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageServiceImpl implements ImageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private final ImageRepository imageRepository;
    private final FileStorage fileStorage;

    public ImageServiceImpl(ImageRepository imageRepository, FileStorage fileStorage) {
        this.imageRepository = imageRepository;
        this.fileStorage = fileStorage;
    }

    @Override
    @Transactional
    public PostFileUploadResponse uploadPostImage(MultipartFile file) {
        Image image = saveImage(file, ImageSubDir.POST_IMAGES);
        return new PostFileUploadResponse(image.getFileUrl());
    }

    @Override
    @Transactional
    public ProfileImageUploadResponse uploadProfileImage(MultipartFile file) {
        Image image = saveImage(file, ImageSubDir.PROFILE_IMAGES);
        return new ProfileImageUploadResponse(image.getFileUrl());
    }

    private Image saveImage(MultipartFile file, ImageSubDir subDir) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(ErrorCode.EMPTY_FILE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ApiException(ErrorCode.INVALID_IMAGE_FORMAT);
        }

        StoredFileInfo stored = fileStorage.store(file, subDir);

        String originalFilename = file.getOriginalFilename();
        Image image = Image.of(
                originalFilename != null ? originalFilename : stored.storedFilename(),
                stored.storedFilename(),
                stored.filePath(),
                stored.fileUrl(),
                file.getSize(),
                contentType
        );
        return imageRepository.save(image);
    }
}
