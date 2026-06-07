package com.stocat.amumal.auth.dto;

public record LoginRequest(
        String email,
        String password
) {
}
