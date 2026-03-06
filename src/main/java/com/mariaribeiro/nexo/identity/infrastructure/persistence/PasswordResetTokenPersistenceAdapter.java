package com.mariaribeiro.nexo.identity.infrastructure.persistence;

import com.mariaribeiro.nexo.identity.application.port.CreatePasswordResetTokenPort;
import com.mariaribeiro.nexo.identity.domain.model.PasswordResetToken;

public class PasswordResetTokenPersistenceAdapter implements CreatePasswordResetTokenPort {

    private final SpringDataPasswordResetTokenRepository passwordResetTokenRepository;

    public PasswordResetTokenPersistenceAdapter(SpringDataPasswordResetTokenRepository passwordResetTokenRepository) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Override
    public PasswordResetToken save(PasswordResetToken passwordResetToken) {
        return passwordResetTokenRepository.saveAndFlush(PasswordResetTokenJpaEntity.from(passwordResetToken))
                .toDomain();
    }
}
