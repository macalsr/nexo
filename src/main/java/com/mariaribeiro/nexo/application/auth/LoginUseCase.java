package com.mariaribeiro.nexo.application.auth;

public interface LoginUseCase {

    LoginResult login(LoginCommand command);
}
