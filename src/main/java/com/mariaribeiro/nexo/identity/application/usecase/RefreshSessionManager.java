package com.mariaribeiro.nexo.identity.application.usecase;

import com.mariaribeiro.nexo.identity.application.port.LoadRefreshSessionByTokenHashPort;
import com.mariaribeiro.nexo.identity.application.port.RefreshTokenGeneratorPort;
import com.mariaribeiro.nexo.identity.application.port.RefreshTokenHasherPort;
import com.mariaribeiro.nexo.identity.application.port.RevokeAllRefreshSessionsPort;
import com.mariaribeiro.nexo.identity.application.port.SaveRefreshSessionPort;
import com.mariaribeiro.nexo.identity.domain.model.RefreshSession;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class RefreshSessionManager {

    private final SaveRefreshSessionPort saveRefreshSessionPort;
    private final LoadRefreshSessionByTokenHashPort loadRefreshSessionByTokenHashPort;
    private final RevokeAllRefreshSessionsPort revokeAllRefreshSessionsPort;
    private final RefreshTokenGeneratorPort refreshTokenGeneratorPort;
    private final RefreshTokenHasherPort refreshTokenHasherPort;
    private final Clock clock;
    private final Duration refreshTokenTtl;

    public RefreshSessionManager(
            SaveRefreshSessionPort saveRefreshSessionPort,
            LoadRefreshSessionByTokenHashPort loadRefreshSessionByTokenHashPort,
            RevokeAllRefreshSessionsPort revokeAllRefreshSessionsPort,
            RefreshTokenGeneratorPort refreshTokenGeneratorPort,
            RefreshTokenHasherPort refreshTokenHasherPort,
            Clock clock,
            Duration refreshTokenTtl) {
        this.saveRefreshSessionPort = saveRefreshSessionPort;
        this.loadRefreshSessionByTokenHashPort = loadRefreshSessionByTokenHashPort;
        this.revokeAllRefreshSessionsPort = revokeAllRefreshSessionsPort;
        this.refreshTokenGeneratorPort = refreshTokenGeneratorPort;
        this.refreshTokenHasherPort = refreshTokenHasherPort;
        this.clock = clock;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    public String issue(UUID userId) {
        String rawToken = refreshTokenGeneratorPort.generate();
        Instant now = Instant.now(clock);
        RefreshSession refreshSession = RefreshSession.create(
                userId,
                refreshTokenHasherPort.hash(rawToken),
                now,
                refreshTokenTtl);
        saveRefreshSessionPort.save(refreshSession);
        return rawToken;
    }

    public String rotate(String rawToken) {
        String normalizedToken = normalizeRawToken(rawToken);
        Instant now = Instant.now(clock);
        RefreshSession currentSession = findByRawToken(normalizedToken)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (!currentSession.isActive(now)) {
            revokeCompromisedIfRotated(currentSession, now);
            throw new InvalidRefreshTokenException();
        }

        String newRawToken = refreshTokenGeneratorPort.generate();
        RefreshSession nextSession = RefreshSession.create(
                currentSession.userId(),
                refreshTokenHasherPort.hash(newRawToken),
                now,
                refreshTokenTtl);

        currentSession.markUsed(now);
        currentSession.rotateTo(nextSession.id(), now);

        saveRefreshSessionPort.save(currentSession);
        saveRefreshSessionPort.save(nextSession);

        return newRawToken;
    }

    public UUID validateActiveUserId(String rawToken) {
        String normalizedToken = normalizeRawToken(rawToken);
        Instant now = Instant.now(clock);
        RefreshSession session = findByRawToken(normalizedToken)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (!session.isActive(now)) {
            revokeCompromisedIfRotated(session, now);
            throw new InvalidRefreshTokenException();
        }

        return session.userId();
    }

    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }

        Instant now = Instant.now(clock);
        findByRawToken(rawToken).ifPresent(session -> {
            session.revoke(now);
            saveRefreshSessionPort.save(session);
        });
    }

    public void revokeAll(UUID userId) {
        revokeAllRefreshSessionsPort.revokeAllByUserId(
                Objects.requireNonNull(userId, "userId must not be null"),
                Instant.now(clock));
    }

    private Optional<RefreshSession> findByRawToken(String rawToken) {
        return loadRefreshSessionByTokenHashPort.findByTokenHash(refreshTokenHasherPort.hash(rawToken));
    }

    private String normalizeRawToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidRefreshTokenException();
        }
        return rawToken.trim();
    }

    private void revokeCompromisedIfRotated(RefreshSession session, Instant now) {
        if (session.replacedBySessionId() != null) {
            revokeAllRefreshSessionsPort.revokeAllByUserId(session.userId(), now);
        }
    }
}
