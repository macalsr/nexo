package com.mariaribeiro.nexo.identity.application.auth;

public interface SignupUseCase {

    SignupResult signup(SignupCommand command);
}

