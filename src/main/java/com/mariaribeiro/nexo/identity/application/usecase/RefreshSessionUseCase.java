package com.mariaribeiro.nexo.identity.application.usecase;

public interface RefreshSessionUseCase {

    RefreshSessionResult refresh(RefreshSessionCommand command);
}
