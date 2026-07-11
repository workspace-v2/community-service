package com.stocat.amumal.auth.repository;

import com.stocat.amumal.auth.domain.RefreshToken;
import com.stocat.amumal.auth.domain.RefreshTokenEntry;
import com.stocat.amumal.user.domain.User;
import com.stocat.amumal.user.repository.UserRepository;
import java.util.Optional;

@Deprecated
public class JpaRefreshTokenStore implements RefreshTokenStore {

  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;

  public JpaRefreshTokenStore(
      RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.userRepository = userRepository;
  }

  @Override
  public void save(RefreshTokenEntry entry) {
    User user = userRepository.getReferenceById(entry.userId());
    refreshTokenRepository.save(RefreshToken.of(entry.token(), user, entry.expiresAt()));
  }

  @Override
  public Optional<RefreshTokenEntry> findByToken(String token) {
    return refreshTokenRepository
        .findByToken(token)
        .map(rt -> new RefreshTokenEntry(rt.getToken(), rt.getUser().getId(), rt.getExpiresAt()));
  }

  @Override
  public void deleteByUserId(Long userId) {
    refreshTokenRepository.deleteByUserId(userId);
  }

  @Override
  public void delete(String token) {
    refreshTokenRepository.deleteByToken(token);
  }
}
