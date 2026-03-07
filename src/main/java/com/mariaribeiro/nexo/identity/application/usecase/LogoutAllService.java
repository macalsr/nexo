package com.mariaribeiro.nexo.identity.application.usecase;

public class LogoutAllService implements LogoutAllUseCase {

    private final RefreshSessionManager refreshSessionManager;

    public LogoutAllService(RefreshSessionManager refreshSessionManager) {
        this.refreshSessionManager = refreshSessionManager;
    }

    @Override
    public void logoutAll(RefreshSessionCommand command) {
        var userId = refreshSessionManager.validateActiveUserId(command.refreshToken());
        refreshSessionManager.revokeAll(userId);
    }
}
