package com.stocat.amumal.auth.repository;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.stocat.amumal.auth.TokenConstants;
import com.stocat.amumal.auth.domain.RefreshTokenEntry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class CaffeineRefreshTokenStore implements RefreshTokenStore {

    private final Cache<String, RefreshTokenEntry> cache = Caffeine.newBuilder()
            .expireAfterWrite(TokenConstants.REFRESH_TOKEN_TTL_DAYS, TimeUnit.DAYS)
            .build();

    @Override
    public void save(RefreshTokenEntry entry) {
        cache.put(entry.token(), entry);
    }

    @Override
    public Optional<RefreshTokenEntry> findByToken(String token) {
        return Optional.ofNullable(cache.getIfPresent(token));
    }

    @Override
    public void deleteByUserId(Long userId) {
        cache.asMap().values().removeIf(entry -> entry.userId().equals(userId));
    }

    @Override
    public void delete(String token) {
        cache.invalidate(token);
    }
}
