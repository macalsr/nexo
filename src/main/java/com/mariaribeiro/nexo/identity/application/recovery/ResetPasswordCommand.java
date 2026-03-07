package com.mariaribeiro.nexo.identity.application.recovery;

public record ResetPasswordCommand(String token, String newPassword) {
}

