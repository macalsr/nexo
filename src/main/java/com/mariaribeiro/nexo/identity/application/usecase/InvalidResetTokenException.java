package com.mariaribeiro.nexo.identity.application.usecase;

public class InvalidResetTokenException extends RuntimeException {

    public InvalidResetTokenException() {
        super("Invalid reset token");
    }
}
