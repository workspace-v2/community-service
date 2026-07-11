package com.stocat.amumal.auth.repository;

import com.stocat.amumal.auth.domain.RefreshTokenEntry;
import java.util.Optional;

public interface RefreshTokenStore {

  void save(RefreshTokenEntry entry);

  Optional<RefreshTokenEntry> findByToken(String token);

  void deleteByUserId(Long userId);

  void delete(String token);
}
