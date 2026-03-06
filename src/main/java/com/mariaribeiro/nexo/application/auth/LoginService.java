package com.mariaribeiro.nexo.application.auth;

import com.mariaribeiro.nexo.domain.user.EmailAddress;

public class LoginService implements LoginUseCase {

    private final LoadUserByEmailPort loadUserByEmailPort;
    private final PasswordHashVerifierPort passwordHashVerifierPort;
    private final TokenServicePort tokenServicePort;

    public LoginService(
            LoadUserByEmailPort loadUserByEmailPort,
            PasswordHashVerifierPort passwordHashVerifierPort,
            TokenServicePort tokenServicePort) {
        this.loadUserByEmailPort = loadUserByEmailPort;
        this.passwordHashVerifierPort = passwordHashVerifierPort;
        this.tokenServicePort = tokenServicePort;
    }

    @Override
    public LoginResult login(LoginCommand command) {
        String normalizedEmail = EmailAddress.of(command.email()).value();

        AuthenticatedUserView user = loadUserByEmailPort.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordHashVerifierPort.matches(command.password(), user.passwordHash())) {
            throw new InvalidCredentialsException();
        }

        SessionToken sessionToken = tokenServicePort.issueToken(user.id(), user.email());
        return new LoginResult(sessionToken.value(), sessionToken.expiresAt());
    }
}
