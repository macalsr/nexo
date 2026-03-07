package com.mariaribeiro.nexo.identity.application.port;

public interface RefreshTokenHasherPort {

    String hash(String rawToken);
}
