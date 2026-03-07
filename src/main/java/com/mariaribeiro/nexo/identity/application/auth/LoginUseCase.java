package com.mariaribeiro.nexo.identity.application.auth;

public interface LoginUseCase {

    LoginResult login(LoginCommand command);
}

