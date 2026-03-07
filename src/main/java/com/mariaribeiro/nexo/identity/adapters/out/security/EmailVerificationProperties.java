package com.mariaribeiro.nexo.identity.adapters.out.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth.email-verification")
public class EmailVerificationProperties {

    private Duration tokenTtl = Duration.ofHours(24);

    public Duration getTokenTtl() {
        return tokenTtl;
    }

    public void setTokenTtl(Duration tokenTtl) {
        this.tokenTtl = tokenTtl;
    }
}
