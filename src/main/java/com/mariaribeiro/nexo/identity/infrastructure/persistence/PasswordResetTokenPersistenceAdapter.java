package com.mariaribeiro.nexo.identity.infrastructure.persistence;

import com.mariaribeiro.nexo.identity.application.port.DeletePasswordResetTokenPort;
import com.mariaribeiro.nexo.identity.application.port.CreatePasswordResetTokenPort;
import com.mariaribeiro.nexo.identity.application.port.LoadPasswordResetTokenPort;
import com.mariaribeiro.nexo.identity.domain.model.PasswordResetToken;
import java.util.Optional;
import java.util.UUID;

public class PasswordResetTokenPersistenceAdapter
        implements CreatePasswordResetTokenPort, LoadPasswordResetTokenPort, DeletePasswordResetTokenPort {

    private final SpringDataPasswordResetTokenRepository passwordResetTokenRepository;

    public PasswordResetTokenPersistenceAdapter(SpringDataPasswordResetTokenRepository passwordResetTokenRepository) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Override
    public PasswordResetToken save(PasswordResetToken passwordResetToken) {
        return passwordResetTokenRepository.saveAndFlush(PasswordResetTokenJpaEntity.from(passwordResetToken))
                .toDomain();
    }

    @Override
    public Optional<PasswordResetToken> findByToken(String token) {
        return passwordResetTokenRepository.findByToken(token)
                .map(PasswordResetTokenJpaEntity::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        passwordResetTokenRepository.deleteById(id);
        passwordResetTokenRepository.flush();
    }
}
