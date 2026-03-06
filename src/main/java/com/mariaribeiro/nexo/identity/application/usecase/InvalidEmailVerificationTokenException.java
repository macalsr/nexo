package com.mariaribeiro.nexo.identity.application.usecase;

public class InvalidEmailVerificationTokenException extends RuntimeException {

    public InvalidEmailVerificationTokenException() {
        super("Invalid verification token");
    }
}
