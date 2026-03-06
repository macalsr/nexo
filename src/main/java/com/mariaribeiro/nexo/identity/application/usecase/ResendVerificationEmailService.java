package com.mariaribeiro.nexo.identity.application.usecase;

import com.mariaribeiro.nexo.identity.application.port.LoadUserByEmailPort;
import com.mariaribeiro.nexo.identity.domain.model.EmailAddress;

public class ResendVerificationEmailService implements ResendVerificationEmailUseCase {

    private final LoadUserByEmailPort loadUserByEmailPort;
    private final IssueEmailVerificationUseCase issueEmailVerificationUseCase;

    public ResendVerificationEmailService(
            LoadUserByEmailPort loadUserByEmailPort,
            IssueEmailVerificationUseCase issueEmailVerificationUseCase) {
        this.loadUserByEmailPort = loadUserByEmailPort;
        this.issueEmailVerificationUseCase = issueEmailVerificationUseCase;
    }

    @Override
    public void resend(String email) {
        String normalizedEmail = EmailAddress.of(email).value();
        loadUserByEmailPort.findByEmail(normalizedEmail)
                .ifPresent(issueEmailVerificationUseCase::issueVerificationFor);
    }
}
