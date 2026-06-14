package com.stocat.amumal.auth.service;

import com.stocat.amumal.auth.dto.LoginRequest;
import com.stocat.amumal.auth.dto.LoginResult;
import com.stocat.amumal.auth.dto.TokenResult;
import com.stocat.amumal.user.dto.UserResponse;

public interface AuthService {

    LoginResult login(LoginRequest request);

    TokenResult refreshAccessToken(String refreshToken);

    UserResponse getAuthenticatedUser(Long userId);
}
