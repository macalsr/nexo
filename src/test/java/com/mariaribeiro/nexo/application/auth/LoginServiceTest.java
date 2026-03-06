package com.mariaribeiro.nexo.application.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LoginServiceTest {

    private final LoadUserByEmailPort loadUserByEmailPort = mock(LoadUserByEmailPort.class);
    private final PasswordHashVerifierPort passwordHashVerifierPort = mock(PasswordHashVerifierPort.class);
    private final TokenServicePort tokenServicePort = mock(TokenServicePort.class);
    private final LoginService loginService = new LoginService(
            loadUserByEmailPort,
            passwordHashVerifierPort,
            tokenServicePort);

    @Test
    void returnsTokenForValidCredentials() {
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-03-06T12:00:00Z");
        Instant expiresAt = Instant.parse("2026-03-07T12:00:00Z");
        AuthenticatedUserView user = new AuthenticatedUserView(
                userId,
                "person@example.com",
                "$2a$10$abcdefghijklmnopqrstuv",
                createdAt);

        when(loadUserByEmailPort.findByEmail("person@example.com")).thenReturn(Optional.of(user));
        when(passwordHashVerifierPort.matches("secret123", user.passwordHash())).thenReturn(true);
        when(tokenServicePort.issueToken(userId, "person@example.com"))
                .thenReturn(new SessionToken("token-value", expiresAt));

        LoginResult result = loginService.login(new LoginCommand("Person@Example.com", "secret123"));

        assertThat(result.accessToken()).isEqualTo("token-value");
        assertThat(result.expiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void throwsGenericUnauthorizedWhenUserDoesNotExist() {
        when(loadUserByEmailPort.findByEmail("person@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginService.login(new LoginCommand("person@example.com", "secret123")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void throwsGenericUnauthorizedWhenPasswordDoesNotMatch() {
        UUID userId = UUID.randomUUID();
        AuthenticatedUserView user = new AuthenticatedUserView(
                userId,
                "person@example.com",
                "$2a$10$abcdefghijklmnopqrstuv",
                Instant.parse("2026-03-06T12:00:00Z"));

        when(loadUserByEmailPort.findByEmail("person@example.com")).thenReturn(Optional.of(user));
        when(passwordHashVerifierPort.matches("wrong-password", user.passwordHash())).thenReturn(false);

        assertThatThrownBy(() -> loginService.login(new LoginCommand("person@example.com", "wrong-password")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");
    }
}
