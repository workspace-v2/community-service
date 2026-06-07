package com.stocat.amumal.auth.dto;

public record LoginResult(
        LoginResponse response,
        String refreshToken
) {
}
