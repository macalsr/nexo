package com.mariaribeiro.nexo.identity.application.usecase;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException() {
        super("Unable to create account");
    }
}
