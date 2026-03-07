package com.mariaribeiro.nexo.identity.application.auth;

public interface LogoutUseCase {

    void logout(RefreshSessionCommand command);
}

