package com.stocat.amumal.auth.controller;

import com.stocat.amumal.auth.dto.LoginRequest;
import com.stocat.amumal.auth.dto.LoginResponse;
import com.stocat.amumal.auth.dto.TokenInfo;
import com.stocat.amumal.auth.dto.TokenResult;
import com.stocat.amumal.auth.service.AuthService;
import com.stocat.amumal.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<LoginResponse> login(
            @RequestBody LoginRequest request,
            HttpServletResponse httpResponse
    ) {
        var result = authService.login(request);

        httpResponse.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookie(result.refreshToken()).toString());
        return ApiResponse.of("로그인이 완료되었습니다.", result.response());
    }

    @PostMapping("/refresh-tokens")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<TokenInfo> refreshAccessToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse httpResponse
    ) {
        TokenResult result = authService.refreshAccessToken(refreshToken);

        httpResponse.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookie(result.newRefreshToken()).toString());
        return ApiResponse.of("토큰 재발급이 완료되었습니다.", result.token());
    }

    private ResponseCookie buildRefreshCookie(String token) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(14 * 24 * 60 * 60)
                .sameSite("Strict")
                .build();
    }
}
