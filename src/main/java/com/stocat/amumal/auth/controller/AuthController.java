package com.stocat.amumal.auth.controller;

import com.stocat.amumal.auth.AuthCookieFactory;
import com.stocat.amumal.auth.annotation.AuthUserId;
import com.stocat.amumal.auth.dto.AuthCheckResponse;
import com.stocat.amumal.auth.dto.LoginRequest;
import com.stocat.amumal.auth.dto.LoginResult;
import com.stocat.amumal.auth.dto.LoginResponse;
import com.stocat.amumal.auth.dto.TokenInfo;
import com.stocat.amumal.auth.dto.TokenResult;
import com.stocat.amumal.auth.service.AuthService;
import com.stocat.amumal.common.response.ApiResponse;
import com.stocat.amumal.user.dto.SignUpRequest;
import com.stocat.amumal.user.dto.SignUpResponse;
import com.stocat.amumal.user.dto.UserResponse;
import com.stocat.amumal.user.usecase.SignUpUseCase;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final SignUpUseCase signUpUseCase;
    private final AuthCookieFactory authCookieFactory;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<LoginResponse> login(
            @RequestBody LoginRequest request,
            HttpServletResponse httpResponse
    ) {
        LoginResult result = authService.login(request);

        httpResponse.addHeader(HttpHeaders.SET_COOKIE,
                authCookieFactory.createAccessTokenCookie(result.response().token().accessToken()).toString());
        httpResponse.addHeader(HttpHeaders.SET_COOKIE,
                authCookieFactory.createRefreshTokenCookie(result.refreshToken()).toString());
        return ApiResponse.of("로그인이 완료되었습니다.", result.response());
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SignUpResponse> signUp(@RequestBody SignUpRequest request) {
        return ApiResponse.of("회원가입이 완료되었습니다.", signUpUseCase.execute(request));
    }

    @GetMapping("/check")
    @ResponseStatus(HttpStatus.OK)
    public AuthCheckResponse check(@AuthUserId Long userId) {
        UserResponse user = authService.getAuthenticatedUser(userId);
        return new AuthCheckResponse(null, "인증에 성공했습니다.", user, user.userId());
    }

    @PostMapping("/refresh-tokens")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<TokenInfo> refreshAccessToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse httpResponse
    ) {
        TokenResult result = authService.refreshAccessToken(refreshToken);

        httpResponse.addHeader(HttpHeaders.SET_COOKIE,
                authCookieFactory.createAccessTokenCookie(result.token().accessToken()).toString());
        httpResponse.addHeader(HttpHeaders.SET_COOKIE,
                authCookieFactory.createRefreshTokenCookie(result.newRefreshToken()).toString());
        return ApiResponse.of("토큰 재발급이 완료되었습니다.", result.token());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse httpResponse
    ) {
        authService.logout(refreshToken);
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, authCookieFactory.expireAccessTokenCookie().toString());
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, authCookieFactory.expireRefreshTokenCookie().toString());
        return ApiResponse.of("로그아웃이 완료되었습니다.", null);
    }
}
