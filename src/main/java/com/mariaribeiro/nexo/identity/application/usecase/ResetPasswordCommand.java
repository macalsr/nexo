package com.mariaribeiro.nexo.identity.application.usecase;

public record ResetPasswordCommand(String token, String newPassword) {
}
