package com.stocat.amumal.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenInfo(
    @JsonProperty("access_token") String accessToken, @JsonProperty("expires_in") long expiresIn) {}
