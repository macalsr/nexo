package com.mariaribeiro.nexo.identity.application.auth;

public interface RefreshSessionUseCase {

    RefreshSessionResult refresh(RefreshSessionCommand command);
}

