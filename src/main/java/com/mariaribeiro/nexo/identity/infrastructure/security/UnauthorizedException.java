package com.mariaribeiro.nexo.identity.infrastructure.security;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("Unauthorized");
    }
}
