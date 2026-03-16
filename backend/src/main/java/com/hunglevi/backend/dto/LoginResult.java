package com.hunglevi.backend.dto;

public record LoginResult(AccessTokenResponse accessTokenResponse, String refreshTokenValue) {
}
