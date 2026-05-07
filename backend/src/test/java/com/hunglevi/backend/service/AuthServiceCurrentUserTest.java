package com.hunglevi.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.hunglevi.backend.dto.UserResponse;
import com.hunglevi.backend.entity.User;
import com.hunglevi.backend.exception.UnauthorizedException;
import com.hunglevi.backend.repository.UserRepository;
import com.hunglevi.backend.security.JwtUtil;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceCurrentUserTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_whenAuthenticationMissing_throwsUnauthorized() {
        assertThrows(UnauthorizedException.class, () -> authService.getCurrentUser());
    }

    @Test
    void getCurrentUser_whenPrincipalIsAnonymous_throwsUnauthorized() {
        TestingAuthenticationToken token = new TestingAuthenticationToken("anonymousUser", "n/a", "ROLE_ANONYMOUS");
        SecurityContextHolder.getContext().setAuthentication(token);

        assertThrows(UnauthorizedException.class, () -> authService.getCurrentUser());
    }

    @Test
    void getCurrentUser_whenPrincipalIsUserDetails_returnsUserResponse() {
        var principal = org.springframework.security.core.userdetails.User.withUsername("alice")
                .password("n/a")
                .authorities("ROLE_USER")
                .build();
        TestingAuthenticationToken token = new TestingAuthenticationToken(principal, "n/a", "ROLE_USER");
        SecurityContextHolder.getContext().setAuthentication(token);

        LocalDateTime createdAt = LocalDateTime.now();
        User user = User.builder()
                .id(1L)
                .username("alice")
                .email("alice@example.com")
                .role("USER")
                .password("encoded")
                .createdAt(createdAt)
                .build();

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        UserResponse response = authService.getCurrentUser();

        assertEquals("alice", response.getUsername());
        assertEquals("alice@example.com", response.getEmail());
        assertEquals("USER", response.getRole());
        assertEquals(createdAt, response.getCreatedAt());
    }
}


