package com.mariaribeiro.nexo.identity.application.auth;

public class LogoutService implements LogoutUseCase {

    private final RefreshSessionManager refreshSessionManager;

    public LogoutService(RefreshSessionManager refreshSessionManager) {
        this.refreshSessionManager = refreshSessionManager;
    }

    @Override
    public void logout(RefreshSessionCommand command) {
        refreshSessionManager.revoke(command.refreshToken());
    }
}

