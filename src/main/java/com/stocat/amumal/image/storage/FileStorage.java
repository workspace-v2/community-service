package com.stocat.amumal.image.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {
    StoredFileInfo store(MultipartFile file, String subDir);
}
