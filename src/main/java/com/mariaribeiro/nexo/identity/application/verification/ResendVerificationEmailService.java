package com.mariaribeiro.nexo.identity.application.verification;

import com.mariaribeiro.nexo.identity.application.port.LoadUserByEmailPort;
import com.mariaribeiro.nexo.identity.domain.model.EmailAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResendVerificationEmailService {

    private final LoadUserByEmailPort loadUserByEmailPort;
    private final IssueEmailVerificationService issueEmailVerificationService;

    public void resend(String email) {
        String normalizedEmail = EmailAddress.of(email).value();
        loadUserByEmailPort.findByEmail(normalizedEmail)
                .ifPresent(issueEmailVerificationService::issueVerificationFor);
    }
}

