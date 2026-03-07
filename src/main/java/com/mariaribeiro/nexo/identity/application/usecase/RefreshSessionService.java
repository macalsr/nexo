package com.mariaribeiro.nexo.identity.application.usecase;

import com.mariaribeiro.nexo.identity.application.port.LoadUserByIdPort;
import com.mariaribeiro.nexo.identity.application.port.TokenServicePort;
import java.util.UUID;

public class RefreshSessionService implements RefreshSessionUseCase {

    private final RefreshSessionManager refreshSessionManager;
    private final LoadUserByIdPort loadUserByIdPort;
    private final TokenServicePort tokenServicePort;

    public RefreshSessionService(
            RefreshSessionManager refreshSessionManager,
            LoadUserByIdPort loadUserByIdPort,
            TokenServicePort tokenServicePort) {
        this.refreshSessionManager = refreshSessionManager;
        this.loadUserByIdPort = loadUserByIdPort;
        this.tokenServicePort = tokenServicePort;
    }

    @Override
    public RefreshSessionResult refresh(RefreshSessionCommand command) {
        UUID userId = refreshSessionManager.validateActiveUserId(command.refreshToken());
        String newRefreshToken = refreshSessionManager.rotate(command.refreshToken());

        AuthenticatedUserView user = loadUserByIdPort.findById(userId)
                .orElseThrow(InvalidRefreshTokenException::new);
        SessionToken accessToken = tokenServicePort.issueToken(user.id(), user.email());

        return new RefreshSessionResult(accessToken.value(), accessToken.expiresAt(), newRefreshToken);
    }
}
