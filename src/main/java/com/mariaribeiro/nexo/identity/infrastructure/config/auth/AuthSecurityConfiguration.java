package com.mariaribeiro.nexo.identity.infrastructure.config.auth;

import com.mariaribeiro.nexo.identity.adapters.in.rest.RefreshCookieService;
import com.mariaribeiro.nexo.identity.adapters.in.security.AuthenticationRequestContext;
import com.mariaribeiro.nexo.identity.adapters.in.security.BearerTokenAuthenticationFilter;
import com.mariaribeiro.nexo.identity.adapters.out.security.AuthTokenProperties;
import com.mariaribeiro.nexo.identity.adapters.out.security.BcryptPasswordHashEncoder;
import com.mariaribeiro.nexo.identity.adapters.out.security.BcryptPasswordHashVerifier;
import com.mariaribeiro.nexo.identity.adapters.out.security.EmailVerificationProperties;
import com.mariaribeiro.nexo.identity.adapters.out.security.JwtTokenService;
import com.mariaribeiro.nexo.identity.adapters.out.security.PasswordResetProperties;
import com.mariaribeiro.nexo.identity.adapters.out.security.RefreshTokenProperties;
import com.mariaribeiro.nexo.identity.adapters.out.security.SecureRefreshTokenGenerator;
import com.mariaribeiro.nexo.identity.adapters.out.security.Sha256RefreshTokenHasher;
import com.mariaribeiro.nexo.identity.application.port.PasswordHashEncoderPort;
import com.mariaribeiro.nexo.identity.application.port.PasswordHashVerifierPort;
import com.mariaribeiro.nexo.identity.application.port.RefreshTokenGeneratorPort;
import com.mariaribeiro.nexo.identity.application.port.RefreshTokenHasherPort;
import com.mariaribeiro.nexo.identity.application.port.TokenServicePort;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties({
        AuthTokenProperties.class,
        PasswordResetProperties.class,
        EmailVerificationProperties.class,
        RefreshTokenProperties.class
})
public class AuthSecurityConfiguration {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    PasswordHashEncoderPort passwordHashEncoderPort(PasswordEncoder passwordEncoder) {
        return new BcryptPasswordHashEncoder(passwordEncoder);
    }

    @Bean
    PasswordHashVerifierPort passwordHashVerifierPort(PasswordEncoder passwordEncoder) {
        return new BcryptPasswordHashVerifier(passwordEncoder);
    }

    @Bean
    TokenServicePort tokenServicePort(Clock authClock, AuthTokenProperties properties) {
        return new JwtTokenService(authClock, properties);
    }

    @Bean
    RefreshTokenGeneratorPort refreshTokenGeneratorPort() {
        return new SecureRefreshTokenGenerator();
    }

    @Bean
    RefreshTokenHasherPort refreshTokenHasherPort() {
        return new Sha256RefreshTokenHasher();
    }

    @Bean
    RefreshCookieService refreshCookieService(RefreshTokenProperties refreshTokenProperties) {
        return new RefreshCookieService(refreshTokenProperties);
    }

    @Bean
    AuthenticationRequestContext authenticationRequestContext() {
        return new AuthenticationRequestContext();
    }

    @Bean
    FilterRegistrationBean<BearerTokenAuthenticationFilter> bearerTokenAuthenticationFilter(
            TokenServicePort tokenServicePort,
            AuthenticationRequestContext authenticationRequestContext) {
        FilterRegistrationBean<BearerTokenAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new BearerTokenAuthenticationFilter(tokenServicePort, authenticationRequestContext));
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
