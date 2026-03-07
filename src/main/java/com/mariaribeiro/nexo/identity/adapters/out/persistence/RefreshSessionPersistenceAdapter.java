package com.mariaribeiro.nexo.identity.adapters.out.persistence;

import com.mariaribeiro.nexo.identity.application.port.LoadRefreshSessionByTokenHashPort;
import com.mariaribeiro.nexo.identity.application.port.RevokeAllRefreshSessionsPort;
import com.mariaribeiro.nexo.identity.application.port.SaveRefreshSessionPort;
import com.mariaribeiro.nexo.identity.domain.model.RefreshSession;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class RefreshSessionPersistenceAdapter
        implements SaveRefreshSessionPort, LoadRefreshSessionByTokenHashPort, RevokeAllRefreshSessionsPort {

    private final SpringDataRefreshSessionRepository refreshSessionRepository;

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

