package com.stocat.amumal.image.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConditionalOnProperty(name = "app.upload.storage-type", havingValue = "s3")
/** 운영 환경에서 S3 저장소를 활성화할 때만 AWS S3 클라이언트를 등록한다. */
public class S3StorageConfig {

  @Bean
  /** 필수 설정값이 빠진 상태로 운영 기동하지 않도록 버킷과 리전을 초기에 검증한다. */
  S3Client s3Client(ImageProperties imageProperties) {
    ImageProperties.S3 s3 = imageProperties.getS3();
    Assert.hasText(s3.getBucket(), "app.upload.s3.bucket must not be blank when S3 storage is enabled");
    Assert.hasText(s3.getRegion(), "app.upload.s3.region must not be blank when S3 storage is enabled");

    return S3Client.builder()
        .region(Region.of(s3.getRegion()))
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();
  }
}
