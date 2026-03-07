package com.mariaribeiro.nexo.identity.adapters.out.security;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth.password-reset")
@Getter
@Setter
public class PasswordResetProperties {

    private Duration tokenTtl = Duration.ofMinutes(15);
}

