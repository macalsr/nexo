package com.mariaribeiro.nexo.identity.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RefreshSessionTest {

    @Test
    void isExpiredReturnsTrueAtExactExpirationTime() {
        Instant issuedAt = Instant.parse("2026-03-07T18:00:00Z");
        RefreshSession session = RefreshSession.create(
                UUID.randomUUID(),
                "hash-value",
                issuedAt,
                Duration.ofMinutes(30));

        Instant expiresAt = issuedAt.plus(Duration.ofMinutes(30));
        assertThat(session.isExpired(expiresAt)).isTrue();
    }

    @Test
    void revokeMarksSessionAsRevokedImmediately() {
        Instant issuedAt = Instant.parse("2026-03-07T18:00:00Z");
        Instant revokedAt = Instant.parse("2026-03-07T18:05:00Z");
        RefreshSession session = RefreshSession.create(
                UUID.randomUUID(),
                "hash-value",
                issuedAt,
                Duration.ofDays(30));

        session.revoke(revokedAt);

        assertThat(session.isRevoked()).isTrue();
        assertThat(session.isActive(revokedAt)).isFalse();
    }

    @Test
    void rotateToRevokesSessionAndSetsReplacement() {
        Instant issuedAt = Instant.parse("2026-03-07T18:00:00Z");
        Instant rotatedAt = Instant.parse("2026-03-07T18:10:00Z");
        UUID replacementId = UUID.randomUUID();
        RefreshSession session = RefreshSession.create(
                UUID.randomUUID(),
                "hash-value",
                issuedAt,
                Duration.ofDays(30));

        session.rotateTo(replacementId, rotatedAt);

        assertThat(session.replacedBySessionId()).isEqualTo(replacementId);
        assertThat(session.revokedAt()).isEqualTo(rotatedAt);
        assertThat(session.isActive(rotatedAt)).isFalse();
    }

    @Test
    void rotateToFailsWhenSessionIsNotActive() {
        Instant issuedAt = Instant.parse("2026-03-07T18:00:00Z");
        Instant expiredAt = issuedAt.plus(Duration.ofMinutes(30));
        RefreshSession session = RefreshSession.create(
                UUID.randomUUID(),
                "hash-value",
                issuedAt,
                Duration.ofMinutes(30));

        assertThatThrownBy(() -> session.rotateTo(UUID.randomUUID(), expiredAt))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("session is not active");
    }
}

