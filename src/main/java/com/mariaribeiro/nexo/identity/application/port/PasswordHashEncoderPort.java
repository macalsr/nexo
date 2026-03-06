package com.mariaribeiro.nexo.identity.application.port;

public interface PasswordHashEncoderPort {

    String encode(String rawPassword);
}
