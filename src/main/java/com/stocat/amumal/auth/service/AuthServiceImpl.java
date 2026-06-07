package com.stocat.amumal.auth.service;

import com.stocat.amumal.auth.JwtProvider;
import com.stocat.amumal.auth.domain.RefreshTokenEntry;
import com.stocat.amumal.auth.dto.LoginRequest;
import com.stocat.amumal.auth.dto.LoginResponse;
import com.stocat.amumal.auth.dto.LoginResult;
import com.stocat.amumal.auth.dto.TokenInfo;
import com.stocat.amumal.auth.dto.TokenResult;
import com.stocat.amumal.auth.repository.RefreshTokenStore;
import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.user.domain.User;
import com.stocat.amumal.user.repository.UserRepository;
import com.stocat.amumal.user.validator.UserValidator;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenStore refreshTokenStore;
    private final JwtProvider jwtProvider;
    private final UserValidator userValidator;

    public AuthServiceImpl(
            UserRepository userRepository,
            RefreshTokenStore refreshTokenStore,
            JwtProvider jwtProvider,
            UserValidator userValidator
    ) {
        this.userRepository = userRepository;
        this.refreshTokenStore = refreshTokenStore;
        this.jwtProvider = jwtProvider;
        this.userValidator = userValidator;
    }

    @Override
    @Transactional
    public LoginResult login(LoginRequest request) {
        userValidator.validateEmail(request.email());
        userValidator.validatePassword(request.password());

        User user = userRepository.findByEmail(request.email().trim())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_CREDENTIALS));

        if (!user.getPassword().equals(request.password())) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 액세스 토큰 발급
        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getNickname());

        // 리프레시 토큰 발급 후 DB 저장
        String refreshTokenValue = jwtProvider.createRefreshToken(user.getId());
        refreshTokenStore.deleteByUserId(user.getId());
        refreshTokenStore.save(
                new RefreshTokenEntry(refreshTokenValue, user.getId(), LocalDateTime.now().plusDays(14)));

        // 응답 바디(LoginResponse)와 쿠키용 리프레시 토큰을 분리해 반환
        long expiresIn = jwtProvider.getAccessTokenValidityInMilliseconds();
        LoginResponse response = new LoginResponse(user.getId(), new TokenInfo(accessToken, expiresIn));
        return new LoginResult(response, refreshTokenValue);
    }

    @Override
    @Transactional
    public TokenResult refreshAccessToken(String refreshToken) {
        // 쿠키에 리프레시 토큰이 없으면 인증 실패
        if (refreshToken == null) {
            throw new ApiException(ErrorCode.INVALID_TOKEN);
        }

        // 저장소에서 토큰 조회 → 없으면 탈취 또는 미발급
        RefreshTokenEntry saved = refreshTokenStore.findByToken(refreshToken)
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_TOKEN));

        // 만료된 토큰이면 저장소에서 삭제 후 인증 실패
        if (saved.isExpired()) {
            refreshTokenStore.delete(refreshToken);
            throw new ApiException(ErrorCode.INVALID_TOKEN);
        }

        // 새 액세스 토큰 발급
        User user = userRepository.findById(saved.userId())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_TOKEN));
        String newAccessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getNickname());

        // RTR: 기존 리프레시 토큰 폐기 후 새 토큰 발급·저장
        String newRefreshTokenValue = jwtProvider.createRefreshToken(user.getId());
        refreshTokenStore.delete(refreshToken);
        refreshTokenStore.save(
                new RefreshTokenEntry(newRefreshTokenValue, user.getId(), LocalDateTime.now().plusDays(14)));

        // 새 액세스 토큰(응답 바디)과 새 리프레시 토큰(쿠키 교체용)을 분리해 반환
        long expiresIn = jwtProvider.getAccessTokenValidityInMilliseconds();
        return new TokenResult(new TokenInfo(newAccessToken, expiresIn), newRefreshTokenValue);
    }
}
