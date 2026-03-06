package com.mariaribeiro.nexo.identity.application.usecase;

import com.mariaribeiro.nexo.identity.application.port.CreateUserPort;
import com.mariaribeiro.nexo.identity.application.port.PasswordHashEncoderPort;
import com.mariaribeiro.nexo.identity.application.port.TokenServicePort;
import com.mariaribeiro.nexo.identity.domain.model.EmailAddress;
import com.mariaribeiro.nexo.identity.domain.model.User;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public class SignupService implements SignupUseCase {

    private final CreateUserPort createUserPort;
    private final PasswordHashEncoderPort passwordHashEncoderPort;
    private final TokenServicePort tokenServicePort;
    private final Clock clock;

    public SignupService(
            CreateUserPort createUserPort,
            PasswordHashEncoderPort passwordHashEncoderPort,
            TokenServicePort tokenServicePort,
            Clock clock) {
        this.createUserPort = createUserPort;
        this.passwordHashEncoderPort = passwordHashEncoderPort;
        this.tokenServicePort = tokenServicePort;
        this.clock = clock;
    }

    @Override
    public SignupResult signup(SignupCommand command) {
        String normalizedEmail = EmailAddress.of(command.email()).value();
        String passwordHash = passwordHashEncoderPort.encode(command.password());

        User createdUser = createUserPort.create(User.create(
                UUID.randomUUID(),
                normalizedEmail,
                passwordHash,
                Instant.now(clock)));

        SessionToken sessionToken = tokenServicePort.issueToken(createdUser.id(), createdUser.email().value());
        return new SignupResult(sessionToken.value(), sessionToken.expiresAt());
    }
}
