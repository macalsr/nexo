package com.mariaribeiro.nexo.identity.application.auth;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException() {
        super("Unable to create account");
    }
}

