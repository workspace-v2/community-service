package com.stocat.amumal.auth.service;

import com.stocat.amumal.auth.dto.LoginRequest;
import com.stocat.amumal.auth.dto.LoginResult;
import com.stocat.amumal.auth.dto.TokenResult;

public interface AuthService {

    LoginResult login(LoginRequest request);

    TokenResult refreshAccessToken(String refreshToken);
}
