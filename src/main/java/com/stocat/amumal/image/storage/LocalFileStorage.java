package com.stocat.amumal.image.storage;

import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.image.config.ImageProperties;
import com.stocat.amumal.image.domain.ImageSubDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class LocalFileStorage implements FileStorage {

    private final ImageProperties imageProperties;

    @Override
    public StoredFileInfo store(MultipartFile file, ImageSubDir subDir) {
        // 원본 파일명에서 확장자 추출 후 UUID 기반 저장 파일명 생성
        // 원본 파일명을 그대로 쓰면 중복 및 보안 문제가 생기므로 UUID로 대체
        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        String storedFilename = UUID.randomUUID() + extension;

        // baseDir + subDir 조합으로 실제 저장 디렉토리 경로 생성
        Path uploadDir = Paths.get(imageProperties.getBaseDir(), subDir.getValue());

        try {
            // 디렉토리가 없으면 생성, 이미 있으면 무시
            Files.createDirectories(uploadDir);

            // 저장 디렉토리 + 저장 파일명으로 최종 파일 경로 확정
            Path filePath = uploadDir.resolve(storedFilename);

            // 업로드된 파일을 위 경로에 실제로 저장
            file.transferTo(filePath);

            // 클라이언트가 접근할 URL 생성
            // WebMvcConfig의 리소스 핸들러가 /images/** 를 uploadDir로 매핑하므로 컨텍스트 패스(/v1)를 포함한 경로로 반환
            String fileUrl = "/v1/images/" + subDir.getValue() + "/" + storedFilename;

            return new StoredFileInfo(storedFilename, filePath.toAbsolutePath().toString(), fileUrl);
        } catch (IOException e) {
            throw new ApiException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    // 파일명에서 확장자 추출, 없으면 빈 문자열 반환
    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
