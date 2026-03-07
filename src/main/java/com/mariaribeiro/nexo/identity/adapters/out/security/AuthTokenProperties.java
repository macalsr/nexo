package com.mariaribeiro.nexo.identity.adapters.out.security;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth.jwt")
@Getter
@Setter
public class AuthTokenProperties {

    private String secret = "change-this-local-dev-jwt-secret-key-1234567890";
    private Duration accessTokenTtl = Duration.ofHours(24);
}

