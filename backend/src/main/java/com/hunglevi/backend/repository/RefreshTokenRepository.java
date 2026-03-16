package com.hunglevi.backend.repository;

import com.hunglevi.backend.entity.RefreshToken;
import com.hunglevi.backend.entity.User;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Query("select count(r) from RefreshToken r where r.user = :user and r.revoked = false and r.expiresAt > :now")
    long countActiveByUser(@Param("user") User user, @Param("now") Instant now);

    @Modifying
    @Transactional
    @Query("update RefreshToken r set r.revoked = true where r.user = :user and r.revoked = false")
    void revokeAllByUser(@Param("user") User user);

    @Modifying
    @Transactional
    @Query("delete from RefreshToken r where r.revoked = true or r.expiresAt <= :now")
    void deleteExpiredAndRevoked(@Param("now") Instant now);
}
