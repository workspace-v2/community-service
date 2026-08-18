package com.stocat.amumal.image.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.upload")
/**
 * 이미지 업로드 저장소 설정
 *
 * <p>기본값은 로컬 디스크 저장소이며, 운영 환경에서는 {@code storageType=s3}로 전환하여 S3 관련 하위 설정을 사용한다.
 */
public class ImageProperties {
  private final S3 s3 = new S3();
  private String storageType = "local";
  private String baseDir = "./uploads";

  @Getter
  @Setter
  /** S3 업로드 저장소에 필요한 버킷, 리전, 객체 키 접두사, 공개 URL 규칙을 정의한다. */
  public static class S3 {
    private String bucket;
    private String region;
    private String prefix = "";
    private String publicBaseUrl;
  }
}
