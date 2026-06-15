package com.stocat.amumal.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthCookieFactory {

    private final CookieProperties cookieProperties;

    public ResponseCookie createAccessTokenCookie(String token) {
        return ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .path("/")
                .maxAge(5 * 60)
                .sameSite("Lax")
                .build();
    }

    public ResponseCookie createRefreshTokenCookie(String token) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .path("/")
                .maxAge(14 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();
    }

    public ResponseCookie expireAccessTokenCookie() {
        return expireCookie("accessToken");
    }

    public ResponseCookie expireRefreshTokenCookie() {
        return expireCookie("refreshToken");
    }

    private ResponseCookie expireCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
    }
}
