package com.mariaribeiro.nexo.identity.infrastructure.config;

import com.mariaribeiro.nexo.identity.application.port.CreateUserPort;
import com.mariaribeiro.nexo.identity.application.port.LoadUserByEmailPort;
import com.mariaribeiro.nexo.identity.application.port.PasswordHashEncoderPort;
import com.mariaribeiro.nexo.identity.application.port.PasswordHashVerifierPort;
import com.mariaribeiro.nexo.identity.application.port.TokenServicePort;
import com.mariaribeiro.nexo.identity.application.usecase.LoginService;
import com.mariaribeiro.nexo.identity.application.usecase.LoginUseCase;
import com.mariaribeiro.nexo.identity.application.usecase.SignupService;
import com.mariaribeiro.nexo.identity.application.usecase.SignupUseCase;
import com.mariaribeiro.nexo.identity.infrastructure.persistence.SpringDataUserRepository;
import com.mariaribeiro.nexo.identity.infrastructure.persistence.UserPersistenceAdapter;
import com.mariaribeiro.nexo.identity.infrastructure.security.AuthenticationRequestContext;
import com.mariaribeiro.nexo.identity.infrastructure.security.AuthTokenProperties;
import com.mariaribeiro.nexo.identity.infrastructure.security.BearerTokenAuthenticationFilter;
import com.mariaribeiro.nexo.identity.infrastructure.security.BcryptPasswordHashEncoder;
import com.mariaribeiro.nexo.identity.infrastructure.security.BcryptPasswordHashVerifier;
import com.mariaribeiro.nexo.identity.infrastructure.security.JwtTokenService;
import java.time.Clock;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties(AuthTokenProperties.class)
public class AuthConfiguration {

    @Bean
    Clock authClock() {
        return Clock.systemUTC();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserPersistenceAdapter userPersistenceAdapter(SpringDataUserRepository userRepository) {
        return new UserPersistenceAdapter(userRepository);
    }

    @Bean
    LoadUserByEmailPort loadUserByEmailPort(UserPersistenceAdapter userPersistenceAdapter) {
        return userPersistenceAdapter;
    }

    @Bean
    CreateUserPort createUserPort(UserPersistenceAdapter userPersistenceAdapter) {
        return userPersistenceAdapter;
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

    @Bean
    LoginUseCase loginUseCase(
            LoadUserByEmailPort loadUserByEmailPort,
            PasswordHashVerifierPort passwordHashVerifierPort,
            TokenServicePort tokenServicePort) {
        return new LoginService(loadUserByEmailPort, passwordHashVerifierPort, tokenServicePort);
    }

    @Bean
    SignupUseCase signupUseCase(
            CreateUserPort createUserPort,
            PasswordHashEncoderPort passwordHashEncoderPort,
            TokenServicePort tokenServicePort,
            Clock authClock) {
        return new SignupService(createUserPort, passwordHashEncoderPort, tokenServicePort, authClock);
    }
}
