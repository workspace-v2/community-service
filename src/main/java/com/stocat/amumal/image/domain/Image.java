package com.stocat.amumal.image.domain;

import com.stocat.amumal.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "image_id")
  private Long id;

  @Column(nullable = false)
  private String originalFilename;

  @Column(nullable = false)
  private String storedFilename;

  @Column(nullable = false, length = 500)
  private String filePath;

  @Column(nullable = false, length = 500)
  private String fileUrl;

  @Column(nullable = false)
  private Long fileSize;

  @Column(nullable = false, length = 100)
  private String contentType;

  public static Image of(
      String originalFilename,
      String storedFilename,
      String filePath,
      String fileUrl,
      Long fileSize,
      String contentType) {
    Image image = new Image();
    image.originalFilename = originalFilename;
    image.storedFilename = storedFilename;
    image.filePath = filePath;
    image.fileUrl = fileUrl;
    image.fileSize = fileSize;
    image.contentType = contentType;
    return image;
  }
}
