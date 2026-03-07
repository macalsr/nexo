package com.mariaribeiro.nexo.identity.application.auth;

import com.mariaribeiro.nexo.identity.application.port.LoadUserByIdPort;
import com.mariaribeiro.nexo.identity.application.port.TokenServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshSessionService {

    private final RefreshSessionManager refreshSessionManager;
    private final LoadUserByIdPort loadUserByIdPort;
    private final TokenServicePort tokenServicePort;

    public RefreshSessionResult refresh(RefreshSessionCommand command) {
        RefreshRotationResult rotationResult = refreshSessionManager.rotateAndResolve(command.refreshToken());

        AuthenticatedUserView user = loadUserByIdPort.findById(rotationResult.userId())
                .orElseThrow(InvalidRefreshTokenException::new);
        SessionToken accessToken = tokenServicePort.issueToken(user.id(), user.email());

        return new RefreshSessionResult(accessToken.value(), accessToken.expiresAt(), rotationResult.refreshToken());
    }
}

