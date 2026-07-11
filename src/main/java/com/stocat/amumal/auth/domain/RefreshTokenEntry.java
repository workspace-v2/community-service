package com.stocat.amumal.auth.domain;

import java.time.LocalDateTime;

public record RefreshTokenEntry(String token, Long userId, LocalDateTime expiresAt) {

  public boolean isExpired() {
    return LocalDateTime.now().isAfter(expiresAt);
  }
}
