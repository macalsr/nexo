package com.mariaribeiro.nexo.identity.application.auth;

import com.mariaribeiro.nexo.identity.application.port.CreateUserPort;
import com.mariaribeiro.nexo.identity.application.port.PasswordHashEncoderPort;
import com.mariaribeiro.nexo.identity.application.port.TokenServicePort;
import com.mariaribeiro.nexo.identity.application.verification.IssueEmailVerificationService;
import com.mariaribeiro.nexo.identity.domain.model.EmailAddress;
import com.mariaribeiro.nexo.identity.domain.model.User;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final CreateUserPort createUserPort;
    private final PasswordHashEncoderPort passwordHashEncoderPort;
    private final TokenServicePort tokenServicePort;
    private final IssueEmailVerificationService issueEmailVerificationService;
    private final RefreshSessionManager refreshSessionManager;
    private final Clock clock;

    public SignupResult signup(SignupCommand command) {
        String normalizedEmail = EmailAddress.of(command.email()).value();
        String passwordHash = passwordHashEncoderPort.encode(command.password());

        User createdUser = createUserPort.create(User.create(
                UUID.randomUUID(),
                normalizedEmail,
                passwordHash,
                Instant.now(clock)));

        issueEmailVerificationService.issueVerificationFor(new AuthenticatedUserView(
                createdUser.id(),
                createdUser.email().value(),
                createdUser.passwordHash().value(),
                createdUser.emailVerified(),
                createdUser.emailVerifiedAt(),
                createdUser.createdAt()));

        SessionToken sessionToken = tokenServicePort.issueToken(createdUser.id(), createdUser.email().value());
        String refreshToken = refreshSessionManager.issue(createdUser.id());
        return new SignupResult(sessionToken.value(), sessionToken.expiresAt(), refreshToken);
    }
}

