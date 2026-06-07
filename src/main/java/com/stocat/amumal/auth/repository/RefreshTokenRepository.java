package com.stocat.amumal.auth.repository;

import com.stocat.amumal.auth.domain.RefreshToken;
import com.stocat.amumal.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}
