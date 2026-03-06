package com.mariaribeiro.nexo.identity.application.port;

public interface PasswordHashVerifierPort {

    boolean matches(String rawPassword, String passwordHash);
}
