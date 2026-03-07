package com.mariaribeiro.nexo.identity.application.auth;

import com.mariaribeiro.nexo.identity.application.port.LoadUserByEmailPort;
import com.mariaribeiro.nexo.identity.application.port.PasswordHashVerifierPort;
import com.mariaribeiro.nexo.identity.application.port.TokenServicePort;
import com.mariaribeiro.nexo.identity.domain.model.EmailAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final LoadUserByEmailPort loadUserByEmailPort;
    private final PasswordHashVerifierPort passwordHashVerifierPort;
    private final TokenServicePort tokenServicePort;
    private final RefreshSessionManager refreshSessionManager;

    public LoginResult login(LoginCommand command) {
        String normalizedEmail = EmailAddress.of(command.email()).value();

        AuthenticatedUserView user = loadUserByEmailPort.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordHashVerifierPort.matches(command.password(), user.passwordHash())) {
            throw new InvalidCredentialsException();
        }

        SessionToken sessionToken = tokenServicePort.issueToken(user.id(), user.email());
        String refreshToken = refreshSessionManager.issue(user.id());
        return new LoginResult(sessionToken.value(), sessionToken.expiresAt(), refreshToken);
    }
}

