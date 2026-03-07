package com.mariaribeiro.nexo.identity.infrastructure.config.auth;

import com.mariaribeiro.nexo.identity.adapters.out.security.RefreshTokenProperties;
import com.mariaribeiro.nexo.identity.application.auth.RefreshSessionManager;
import com.mariaribeiro.nexo.identity.application.port.LoadRefreshSessionByTokenHashPort;
import com.mariaribeiro.nexo.identity.application.port.RefreshTokenGeneratorPort;
import com.mariaribeiro.nexo.identity.application.port.RefreshTokenHasherPort;
import com.mariaribeiro.nexo.identity.application.port.RevokeAllRefreshSessionsPort;
import com.mariaribeiro.nexo.identity.application.port.SaveRefreshSessionPort;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthApplicationConfiguration {

    @Bean
    Clock authClock() {
        return Clock.systemUTC();
    }

    @Bean
    RefreshSessionManager refreshSessionManager(
            SaveRefreshSessionPort saveRefreshSessionPort,
            LoadRefreshSessionByTokenHashPort loadRefreshSessionByTokenHashPort,
            RevokeAllRefreshSessionsPort revokeAllRefreshSessionsPort,
            RefreshTokenGeneratorPort refreshTokenGeneratorPort,
            RefreshTokenHasherPort refreshTokenHasherPort,
            Clock authClock,
            RefreshTokenProperties refreshTokenProperties) {
        return new RefreshSessionManager(
                saveRefreshSessionPort,
                loadRefreshSessionByTokenHashPort,
                revokeAllRefreshSessionsPort,
                refreshTokenGeneratorPort,
                refreshTokenHasherPort,
                authClock,
                refreshTokenProperties.getTtl());
    }
}
