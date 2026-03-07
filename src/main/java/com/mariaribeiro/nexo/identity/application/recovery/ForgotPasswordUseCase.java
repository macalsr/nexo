package com.mariaribeiro.nexo.identity.application.recovery;

public interface ForgotPasswordUseCase {

    void requestReset(ForgotPasswordCommand command);
}

