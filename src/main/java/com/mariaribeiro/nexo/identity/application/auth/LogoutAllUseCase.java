package com.mariaribeiro.nexo.identity.application.auth;

public interface LogoutAllUseCase {

    void logoutAll(RefreshSessionCommand command);
}

