package com.mariaribeiro.nexo.identity.adapters.out.security;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("Unauthorized");
    }
}
