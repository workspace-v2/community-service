package com.stocat.amumal.image.storage;

import com.stocat.amumal.image.domain.ImageSubDir;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {
  StoredFileInfo store(MultipartFile file, ImageSubDir subDir);
}
