package com.mariaribeiro.nexo.application.auth;

public interface PasswordHashVerifierPort {

    boolean matches(String rawPassword, String passwordHash);
}
