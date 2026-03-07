package com.mariaribeiro.nexo.identity.adapters.out.persistence;

import com.mariaribeiro.nexo.identity.application.port.LoadRefreshSessionByTokenHashPort;
import com.mariaribeiro.nexo.identity.application.port.RevokeAllRefreshSessionsPort;
import com.mariaribeiro.nexo.identity.application.port.SaveRefreshSessionPort;
import com.mariaribeiro.nexo.identity.domain.model.RefreshSession;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class RefreshSessionPersistenceAdapter
        implements SaveRefreshSessionPort, LoadRefreshSessionByTokenHashPort, RevokeAllRefreshSessionsPort {

    private final SpringDataRefreshSessionRepository refreshSessionRepository;

    public RefreshSessionPersistenceAdapter(SpringDataRefreshSessionRepository refreshSessionRepository) {
        this.refreshSessionRepository = refreshSessionRepository;
    }

    @Override
    public RefreshSession save(RefreshSession refreshSession) {
        return refreshSessionRepository.saveAndFlush(RefreshSessionJpaEntity.from(refreshSession))
                .toDomain();
    }

    @Override
    public Optional<RefreshSession> findByTokenHash(String tokenHash) {
        return refreshSessionRepository.findByTokenHash(tokenHash)
                .map(RefreshSessionJpaEntity::toDomain);
    }

    @Override
    @Transactional
    public void revokeAllByUserId(UUID userId, Instant revokedAt) {
        refreshSessionRepository.revokeAllActiveByUserId(userId, revokedAt);
    }
}
