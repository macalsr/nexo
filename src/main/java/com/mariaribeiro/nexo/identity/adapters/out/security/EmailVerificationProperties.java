package com.mariaribeiro.nexo.identity.adapters.out.security;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth.email-verification")
@Getter
@Setter
public class EmailVerificationProperties {

    private Duration tokenTtl = Duration.ofHours(24);
}

