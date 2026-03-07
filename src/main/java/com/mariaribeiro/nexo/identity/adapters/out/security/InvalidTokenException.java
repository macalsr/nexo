package com.mariaribeiro.nexo.identity.adapters.out.security;

public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
