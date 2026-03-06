package com.mariaribeiro.nexo.identity.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mariaribeiro.nexo.identity.application.usecase.TokenClaims;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTest {

    private AuthTokenProperties properties(Duration duration) {
        AuthTokenProperties properties = new AuthTokenProperties();
        properties.setSecret("change-this-local-dev-jwt-secret-key-1234567890");
        properties.setAccessTokenTtl(duration);
        return properties;
    }

    @Test
    void verifiesIssuedTokenBeforeExpiry() {
        Clock clock = Clock.fixed(Instant.parse("2026-03-06T12:00:00Z"), ZoneOffset.UTC);
        JwtTokenService tokenService = new JwtTokenService(clock, properties(Duration.ofHours(24)));

        UUID userId = UUID.randomUUID();
        var sessionToken = tokenService.issueToken(userId, "person@example.com");

        TokenClaims claims = tokenService.verify(sessionToken.value());

        assertThat(claims.userId()).isEqualTo(userId);
        assertThat(claims.email()).isEqualTo("person@example.com");
        assertThat(claims.expiresAt()).isEqualTo(Instant.parse("2026-03-07T12:00:00Z"));
    }

    @Test
    void rejectsExpiredTokens() {
        Clock issueClock = Clock.fixed(Instant.parse("2026-03-06T12:00:00Z"), ZoneOffset.UTC);
        JwtTokenService issuingService = new JwtTokenService(issueClock, properties(Duration.ofMinutes(5)));
        String token = issuingService.issueToken(UUID.randomUUID(), "person@example.com").value();

        Clock expiredClock = Clock.fixed(Instant.parse("2026-03-06T12:06:00Z"), ZoneOffset.UTC);
        JwtTokenService verifyingService = new JwtTokenService(expiredClock, properties(Duration.ofMinutes(5)));

        assertThatThrownBy(() -> verifyingService.verify(token))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Token expired");
    }

    @Test
    void rejectsTokensSignedWithDifferentSecret() {
        Clock clock = Clock.fixed(Instant.parse("2026-03-06T12:00:00Z"), ZoneOffset.UTC);
        JwtTokenService issuingService = new JwtTokenService(clock, properties(Duration.ofHours(24)));
        String token = issuingService.issueToken(UUID.randomUUID(), "person@example.com").value();

        AuthTokenProperties differentSecretProperties = new AuthTokenProperties();
        differentSecretProperties.setSecret("another-local-dev-jwt-secret-key-1234567890");
        differentSecretProperties.setAccessTokenTtl(Duration.ofHours(24));
        JwtTokenService verifyingService = new JwtTokenService(clock, differentSecretProperties);

        assertThatThrownBy(() -> verifyingService.verify(token))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Token invalid");
    }
}
