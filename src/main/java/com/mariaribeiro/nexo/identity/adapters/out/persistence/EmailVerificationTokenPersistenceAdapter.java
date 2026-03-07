package com.mariaribeiro.nexo.identity.adapters.out.persistence;

import com.mariaribeiro.nexo.identity.application.port.CreateEmailVerificationTokenPort;
import com.mariaribeiro.nexo.identity.application.port.DeleteEmailVerificationTokenPort;
import com.mariaribeiro.nexo.identity.application.port.LoadEmailVerificationTokenPort;
import com.mariaribeiro.nexo.identity.domain.model.EmailVerificationToken;
import java.util.Optional;
import java.util.UUID;

public class EmailVerificationTokenPersistenceAdapter
        implements CreateEmailVerificationTokenPort, LoadEmailVerificationTokenPort, DeleteEmailVerificationTokenPort {

    private final SpringDataEmailVerificationTokenRepository emailVerificationTokenRepository;

    public EmailVerificationTokenPersistenceAdapter(
            SpringDataEmailVerificationTokenRepository emailVerificationTokenRepository) {
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
    }

    @Override
    public EmailVerificationToken save(EmailVerificationToken emailVerificationToken) {
        return emailVerificationTokenRepository.saveAndFlush(EmailVerificationTokenJpaEntity.from(emailVerificationToken))
                .toDomain();
    }

    @Override
    public Optional<EmailVerificationToken> findByToken(String token) {
        return emailVerificationTokenRepository.findByToken(token)
                .map(EmailVerificationTokenJpaEntity::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        emailVerificationTokenRepository.deleteById(id);
        emailVerificationTokenRepository.flush();
    }

    @Override
    public void deleteByUserId(UUID userId) {
        emailVerificationTokenRepository.deleteByUserId(userId);
        emailVerificationTokenRepository.flush();
    }
}
