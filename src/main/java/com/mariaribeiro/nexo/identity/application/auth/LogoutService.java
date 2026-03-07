package com.mariaribeiro.nexo.identity.application.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final RefreshSessionManager refreshSessionManager;

    public void logout(RefreshSessionCommand command) {
        refreshSessionManager.revoke(command.refreshToken());
    }
}

