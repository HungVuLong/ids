package com.hunglevi.backend.dto;

public record AccessTokenResponse(String token, long expiresIn, String username, String role) {
}
