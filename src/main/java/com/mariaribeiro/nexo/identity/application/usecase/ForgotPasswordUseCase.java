package com.mariaribeiro.nexo.identity.application.usecase;

public interface ForgotPasswordUseCase {

    void requestReset(ForgotPasswordCommand command);
}
