package com.hunglevi.backend.service;

import com.hunglevi.backend.dto.AccessTokenResponse;
import com.hunglevi.backend.dto.LoginRequest;
import com.hunglevi.backend.dto.LoginResult;
import com.hunglevi.backend.dto.RegisterRequest;
import com.hunglevi.backend.dto.UserResponse;
import com.hunglevi.backend.entity.RefreshToken;
import com.hunglevi.backend.entity.User;
import com.hunglevi.backend.exception.DuplicateResourceException;
import com.hunglevi.backend.exception.UnauthorizedException;
import com.hunglevi.backend.exception.ResourceNotFoundException;
import com.hunglevi.backend.repository.UserRepository;
import com.hunglevi.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private static final long ACCESS_TOKEN_TTL_SECONDS = 900;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;

    public LoginResult login(LoginRequest request, String userAgent) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String accessToken = jwtUtil.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, userAgent);

        log.info("User {} logged in", user.getUsername());
        return new LoginResult(
                new AccessTokenResponse(accessToken, ACCESS_TOKEN_TTL_SECONDS, user.getUsername(), user.getRole()),
                refreshToken.getToken()
        );
    }

    public LoginResult refresh(String refreshTokenValue, String userAgent) {
        RefreshToken refreshToken = refreshTokenService.rotateRefreshToken(refreshTokenValue, userAgent);
        User user = refreshToken.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String accessToken = jwtUtil.generateToken(userDetails);

        log.info("Token refreshed for: {}", user.getUsername());
        return new LoginResult(
                new AccessTokenResponse(accessToken, ACCESS_TOKEN_TTL_SECONDS, user.getUsername(), user.getRole()),
                refreshToken.getToken()
        );
    }

    public void register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .build();

        userRepository.save(user);
        log.info("User {} registered", user.getUsername());
    }

    public void logout(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        refreshTokenService.revokeAllTokens(user);
        log.info("User {} logged out", username);
    }

    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Authentication required");
        }

        Object principal = authentication.getPrincipal();
        String username;
        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else if (principal instanceof String principalName && !"anonymousUser".equals(principalName)) {
            username = principalName;
        } else {
            throw new UnauthorizedException("Authentication required");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
