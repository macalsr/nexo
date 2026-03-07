package com.mariaribeiro.nexo.identity.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mariaribeiro.nexo.identity.application.port.LoadRefreshSessionByTokenHashPort;
import com.mariaribeiro.nexo.identity.application.port.RefreshTokenGeneratorPort;
import com.mariaribeiro.nexo.identity.application.port.RefreshTokenHasherPort;
import com.mariaribeiro.nexo.identity.application.port.RevokeAllRefreshSessionsPort;
import com.mariaribeiro.nexo.identity.application.port.SaveRefreshSessionPort;
import com.mariaribeiro.nexo.identity.domain.model.RefreshSession;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RefreshSessionManagerTest {

    private final SaveRefreshSessionPort saveRefreshSessionPort = mock(SaveRefreshSessionPort.class);
    private final LoadRefreshSessionByTokenHashPort loadRefreshSessionByTokenHashPort = mock(LoadRefreshSessionByTokenHashPort.class);
    private final RevokeAllRefreshSessionsPort revokeAllRefreshSessionsPort = mock(RevokeAllRefreshSessionsPort.class);
    private final RefreshTokenGeneratorPort refreshTokenGeneratorPort = mock(RefreshTokenGeneratorPort.class);
    private final RefreshTokenHasherPort refreshTokenHasherPort = mock(RefreshTokenHasherPort.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-03-07T18:00:00Z"), ZoneOffset.UTC);

    private final RefreshSessionManager manager = new RefreshSessionManager(
            saveRefreshSessionPort,
            loadRefreshSessionByTokenHashPort,
            revokeAllRefreshSessionsPort,
            refreshTokenGeneratorPort,
            refreshTokenHasherPort,
            clock,
            Duration.ofDays(30));

    @Test
    void rotateReplacesCurrentTokenAndReturnsNewRawToken() {
        UUID userId = UUID.randomUUID();
        RefreshSession current = RefreshSession.create(
                userId,
                "hash-old",
                Instant.parse("2026-03-07T17:00:00Z"),
                Duration.ofDays(30));

        when(refreshTokenHasherPort.hash("old-token")).thenReturn("hash-old");
        when(loadRefreshSessionByTokenHashPort.findByTokenHash("hash-old")).thenReturn(Optional.of(current));
        when(refreshTokenGeneratorPort.generate()).thenReturn("new-token");
        when(refreshTokenHasherPort.hash("new-token")).thenReturn("hash-new");
        when(saveRefreshSessionPort.save(any(RefreshSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String rotated = manager.rotate("old-token");

        assertThat(rotated).isEqualTo("new-token");
        assertThat(current.isRevoked()).isTrue();
        assertThat(current.replacedBySessionId()).isNotNull();
        verify(saveRefreshSessionPort).save(current);
    }

    @Test
    void rotateRejectsExpiredToken() {
        UUID userId = UUID.randomUUID();
        RefreshSession expired = RefreshSession.create(
                userId,
                "hash-old",
                Instant.parse("2026-02-01T17:00:00Z"),
                Duration.ofDays(1));

        when(refreshTokenHasherPort.hash("old-token")).thenReturn("hash-old");
        when(loadRefreshSessionByTokenHashPort.findByTokenHash("hash-old")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> manager.rotate("old-token"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("Invalid refresh token");
    }

    @Test
    void rotateDetectsReuseAndRevokesAllSessions() {
        UUID userId = UUID.randomUUID();
        RefreshSession reused = RefreshSession.restore(
                UUID.randomUUID(),
                userId,
                "hash-old",
                Instant.parse("2026-04-01T17:00:00Z"),
                Instant.parse("2026-03-01T17:00:00Z"),
                Instant.parse("2026-03-02T17:00:00Z"),
                Instant.parse("2026-03-03T17:00:00Z"),
                UUID.randomUUID());

        when(refreshTokenHasherPort.hash("old-token")).thenReturn("hash-old");
        when(loadRefreshSessionByTokenHashPort.findByTokenHash("hash-old")).thenReturn(Optional.of(reused));

        assertThatThrownBy(() -> manager.rotate("old-token"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("Invalid refresh token");
        verify(revokeAllRefreshSessionsPort).revokeAllByUserId(eq(userId), any(Instant.class));
        verify(saveRefreshSessionPort, never()).save(any(RefreshSession.class));
    }
}
