package com.mariaribeiro.nexo.application.auth;

public record LoginCommand(String email, String password) {
}
