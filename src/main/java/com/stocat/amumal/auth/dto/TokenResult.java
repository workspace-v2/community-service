package com.stocat.amumal.auth.dto;

public record TokenResult(
        TokenInfo token,       // 응답 바디 (accessToken, expiresIn)
        String newRefreshToken // 회전 시에만 사용 (없으면 null)
) {
}
