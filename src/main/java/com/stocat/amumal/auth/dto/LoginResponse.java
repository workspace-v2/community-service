package com.stocat.amumal.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponse(@JsonProperty("user_id") Long userId, TokenInfo token) {}
