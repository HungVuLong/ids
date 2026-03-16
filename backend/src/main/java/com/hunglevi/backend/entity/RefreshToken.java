package com.hunglevi.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * Stores refresh tokens server-side.
 * One-time use: each use invalidates old token and issues a new one.
 * Enables token revocation and theft detection.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The actual token value — UUID stored as-is (or hashed for extra security) */
    @Column(nullable = false, unique = true, length = 512)
    private String token;

    /** Owner of this token */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** When this token expires (server-side enforcement) */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** True = already used/revoked — cannot be used again */
    @Column(name = "revoked", nullable = false)
    @Builder.Default
    private boolean revoked = false;

    /** Device/browser info for audit */
    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
