package com.stocat.amumal.image.domain;

public enum ImageSubDir {
  POST_IMAGES("post-images"),
  PROFILE_IMAGES("profile-images");

  private final String value;

  ImageSubDir(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
