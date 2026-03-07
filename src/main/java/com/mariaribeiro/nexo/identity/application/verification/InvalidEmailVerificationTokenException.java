package com.mariaribeiro.nexo.identity.application.verification;

public class InvalidEmailVerificationTokenException extends RuntimeException {

    public InvalidEmailVerificationTokenException() {
        super("Invalid verification token");
    }
}

