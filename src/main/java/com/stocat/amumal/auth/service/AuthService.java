package com.stocat.amumal.auth.service;

import com.stocat.amumal.auth.dto.LoginRequest;
import com.stocat.amumal.auth.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}
