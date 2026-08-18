package com.stocat.amumal.image.storage;

import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.image.config.ImageProperties;
import com.stocat.amumal.image.domain.ImageSubDir;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.upload.storage-type", havingValue = "s3")
/**
 * 업로드 파일을 S3 버킷에 저장하는 구현체
 *
 * <p>DB에는 클라이언트가 접근할 공개 URL과 내부 추적용 {@code s3://bucket/key} 경로를 함께 남긴다.
 */
public class S3FileStorage implements FileStorage {

  private final S3Client s3Client;
  private final ImageProperties imageProperties;

  @Override
  public StoredFileInfo store(MultipartFile file, ImageSubDir subDir) {
    String originalFilename = file.getOriginalFilename();
    String extension = extractExtension(originalFilename);
    String storedFilename = UUID.randomUUID() + extension;
    String objectKey = buildObjectKey(subDir, storedFilename);

    try {
      PutObjectRequest request =
          PutObjectRequest.builder()
              .bucket(imageProperties.getS3().getBucket())
              .key(objectKey)
              .contentType(file.getContentType())
              .build();

      s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

      return new StoredFileInfo(
          storedFilename,
          "s3://%s/%s".formatted(imageProperties.getS3().getBucket(), objectKey),
          buildFileUrl(objectKey));
    } catch (Exception e) {
      throw new ApiException(ErrorCode.FILE_UPLOAD_FAILED);
    }
  }

  /** prefix가 있으면 {@code prefix/subdir/filename}, 없으면 {@code subdir/filename} 규칙을 사용한다. */
  private String buildObjectKey(ImageSubDir subDir, String storedFilename) {
    String prefix = imageProperties.getS3().getPrefix();
    if (prefix == null || prefix.isBlank()) {
      return subDir.getValue() + "/" + storedFilename;
    }
    String normalizedPrefix =
        prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
    return normalizedPrefix + "/" + subDir.getValue() + "/" + storedFilename;
  }

  /** CDN 또는 커스텀 도메인이 없으면 S3 기본 퍼블릭 URL 규칙으로 공개 URL을 생성한다. */
  private String buildFileUrl(String objectKey) {
    String publicBaseUrl = imageProperties.getS3().getPublicBaseUrl();
    if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
      String normalizedBaseUrl =
          publicBaseUrl.endsWith("/")
              ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
              : publicBaseUrl;
      return normalizedBaseUrl + "/" + objectKey;
    }
    return "https://%s.s3.%s.amazonaws.com/%s"
        .formatted(
            imageProperties.getS3().getBucket(), imageProperties.getS3().getRegion(), objectKey);
  }

  private String extractExtension(String filename) {
    if (filename == null || !filename.contains(".")) {
      return "";
    }
    return filename.substring(filename.lastIndexOf("."));
  }
}
