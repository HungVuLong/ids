package com.hunglevi.backend.service;

import com.hunglevi.backend.entity.RefreshToken;
import com.hunglevi.backend.entity.User;
import com.hunglevi.backend.exception.InvalidTokenException;
import com.hunglevi.backend.repository.RefreshTokenRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.expiration}")
    private long refreshExpiration;

    public RefreshToken createRefreshToken(User user, String userAgent) {
        Instant now = Instant.now();
        long activeSessions = refreshTokenRepository.countActiveByUser(user, now);
        if (activeSessions >= 5) {
            log.warn("Active refresh sessions >= 5 for user {}, revoking all.", user.getUsername());
            refreshTokenRepository.revokeAllByUser(user);
        }

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(now.plusMillis(refreshExpiration))
                .revoked(false)
                .userAgent(userAgent)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken rotateRefreshToken(String tokenValue, String userAgent) {
        RefreshToken existing = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new InvalidTokenException("not found"));

        if (existing.isRevoked()) {
            log.error("SECURITY: Refresh token REUSE detected for user {}!", existing.getUser().getUsername());
            refreshTokenRepository.revokeAllByUser(existing.getUser());
            throw new InvalidTokenException("Token reuse detected - all sessions invalidated");
        }

        Instant now = Instant.now();
        if (!existing.getExpiresAt().isAfter(now)) {
            existing.setRevoked(true);
            refreshTokenRepository.save(existing);
            throw new InvalidTokenException("expired");
        }

        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        return createRefreshToken(existing.getUser(), userAgent);
    }

    @Transactional
    public void revokeAllTokens(User user) {
        refreshTokenRepository.revokeAllByUser(user);
        log.info("Revoked all refresh tokens for: {}", user.getUsername());
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredAndRevoked(Instant.now());
        log.info("Cleaned up expired/revoked refresh tokens");
    }
}
