package com.mariaribeiro.nexo.identity.application.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutAllService {

    private final RefreshSessionManager refreshSessionManager;

    public void logoutAll(RefreshSessionCommand command) {
        var userId = refreshSessionManager.validateActiveUserId(command.refreshToken());
        refreshSessionManager.revokeAll(userId);
    }
}

