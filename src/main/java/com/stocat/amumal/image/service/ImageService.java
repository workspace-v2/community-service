package com.stocat.amumal.image.service;

import com.stocat.amumal.image.domain.Image;
import com.stocat.amumal.image.domain.ImageSubDir;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    Image upload(MultipartFile file, ImageSubDir subDir);
}
