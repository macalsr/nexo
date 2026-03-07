package com.mariaribeiro.nexo.identity.application.usecase;

public interface LogoutUseCase {

    void logout(RefreshSessionCommand command);
}
