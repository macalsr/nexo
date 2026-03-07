package com.mariaribeiro.nexo.identity.adapters.out.security;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth.refresh-token")
@Getter
@Setter
public class RefreshTokenProperties {

    private Duration ttl = Duration.ofDays(30);
    private String cookieName = "nexo_refresh_token";
    private String cookiePath = "/auth";
    private String cookieSameSite = "Lax";
    private boolean cookieSecure = false;
}

