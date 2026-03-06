package com.mariaribeiro.nexo.identity.application.usecase;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
