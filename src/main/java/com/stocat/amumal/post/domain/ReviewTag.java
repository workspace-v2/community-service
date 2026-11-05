package com.stocat.amumal.post.domain;

import java.util.Arrays;
import java.util.List;

public enum ReviewTag {
  SPICY("매콤"),
  THICK_PATTY("두꺼운 패티"),
  GOOD_VALUE("가성비"),
  CHEESY("치즈"),
  JUICY("육즙"),
  CRISPY("바삭"),
  HANDMADE("수제버거");
  private final String label;

  ReviewTag(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public static List<ReviewTag> matching(String keyword) {
    return Arrays.stream(values())
        .filter(t -> t.label.contains(keyword) || t.name().equalsIgnoreCase(keyword))
        .toList();
  }
}
