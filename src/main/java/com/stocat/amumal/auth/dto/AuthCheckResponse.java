package com.stocat.amumal.auth.dto;

import com.stocat.amumal.user.dto.UserResponse;

public record AuthCheckResponse(String code, String message, UserResponse data, Long idx) {}
