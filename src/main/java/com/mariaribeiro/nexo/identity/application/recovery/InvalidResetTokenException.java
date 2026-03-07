package com.mariaribeiro.nexo.identity.application.recovery;

public class InvalidResetTokenException extends RuntimeException {

    public InvalidResetTokenException() {
        super("Invalid reset token");
    }
}

