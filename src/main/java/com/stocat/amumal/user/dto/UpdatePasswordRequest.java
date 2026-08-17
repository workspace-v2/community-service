package com.stocat.amumal.user.dto;

public record UpdatePasswordRequest(
    String currentPassword, String password, String passwordConfirm) {}
