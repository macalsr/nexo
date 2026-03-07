package com.mariaribeiro.nexo.identity.infrastructure.config;

import com.mariaribeiro.nexo.identity.application.port.CreateUserPort;
import com.mariaribeiro.nexo.identity.application.port.CreateEmailVerificationTokenPort;
import com.mariaribeiro.nexo.identity.application.port.CreatePasswordResetTokenPort;
import com.mariaribeiro.nexo.identity.application.port.DeleteEmailVerificationTokenPort;
import com.mariaribeiro.nexo.identity.application.port.DeletePasswordResetTokenPort;
import com.mariaribeiro.nexo.identity.application.port.EmailVerificationDeliveryPort;
import com.mariaribeiro.nexo.identity.application.port.LoadEmailVerificationTokenPort;
import com.mariaribeiro.nexo.identity.application.port.LoadRefreshSessionByTokenHashPort;
import com.mariaribeiro.nexo.identity.application.port.LoadUserByEmailPort;
import com.mariaribeiro.nexo.identity.application.port.LoadUserByIdPort;
import com.mariaribeiro.nexo.identity.application.port.LoadPasswordResetTokenPort;
import com.mariaribeiro.nexo.identity.application.port.MarkUserEmailVerifiedPort;
import com.mariaribeiro.nexo.identity.application.port.PasswordHashEncoderPort;
import com.mariaribeiro.nexo.identity.application.port.PasswordHashVerifierPort;
import com.mariaribeiro.nexo.identity.application.port.PasswordResetDeliveryPort;
import com.mariaribeiro.nexo.identity.application.port.RefreshTokenGeneratorPort;
import com.mariaribeiro.nexo.identity.application.port.RefreshTokenHasherPort;
import com.mariaribeiro.nexo.identity.application.port.RevokeAllRefreshSessionsPort;
import com.mariaribeiro.nexo.identity.application.port.SaveRefreshSessionPort;
import com.mariaribeiro.nexo.identity.application.port.TokenServicePort;
import com.mariaribeiro.nexo.identity.application.port.UpdateUserPasswordPort;
import com.mariaribeiro.nexo.identity.application.usecase.ForgotPasswordService;
import com.mariaribeiro.nexo.identity.application.usecase.ForgotPasswordUseCase;
import com.mariaribeiro.nexo.identity.application.usecase.IssueEmailVerificationService;
import com.mariaribeiro.nexo.identity.application.usecase.IssueEmailVerificationUseCase;
import com.mariaribeiro.nexo.identity.application.usecase.LoginService;
import com.mariaribeiro.nexo.identity.application.usecase.LoginUseCase;
import com.mariaribeiro.nexo.identity.application.usecase.LogoutAllService;
import com.mariaribeiro.nexo.identity.application.usecase.LogoutAllUseCase;
import com.mariaribeiro.nexo.identity.application.usecase.LogoutService;
import com.mariaribeiro.nexo.identity.application.usecase.LogoutUseCase;
import com.mariaribeiro.nexo.identity.application.usecase.RefreshSessionManager;
import com.mariaribeiro.nexo.identity.application.usecase.RefreshSessionService;
import com.mariaribeiro.nexo.identity.application.usecase.RefreshSessionUseCase;
import com.mariaribeiro.nexo.identity.application.usecase.ResendVerificationEmailService;
import com.mariaribeiro.nexo.identity.application.usecase.ResendVerificationEmailUseCase;
import com.mariaribeiro.nexo.identity.application.usecase.ResetPasswordService;
import com.mariaribeiro.nexo.identity.application.usecase.ResetPasswordUseCase;
import com.mariaribeiro.nexo.identity.application.usecase.SignupService;
import com.mariaribeiro.nexo.identity.application.usecase.SignupUseCase;
import com.mariaribeiro.nexo.identity.application.usecase.VerifyEmailService;
import com.mariaribeiro.nexo.identity.application.usecase.VerifyEmailUseCase;
import com.mariaribeiro.nexo.identity.adapters.out.notification.StubEmailVerificationDeliveryAdapter;
import com.mariaribeiro.nexo.identity.adapters.out.notification.StubPasswordResetDeliveryAdapter;
import com.mariaribeiro.nexo.identity.adapters.out.persistence.EmailVerificationTokenPersistenceAdapter;
import com.mariaribeiro.nexo.identity.adapters.out.persistence.PasswordResetTokenPersistenceAdapter;
import com.mariaribeiro.nexo.identity.adapters.out.persistence.RefreshSessionPersistenceAdapter;
import com.mariaribeiro.nexo.identity.adapters.out.persistence.SpringDataEmailVerificationTokenRepository;
import com.mariaribeiro.nexo.identity.adapters.out.persistence.SpringDataPasswordResetTokenRepository;
import com.mariaribeiro.nexo.identity.adapters.out.persistence.SpringDataRefreshSessionRepository;
import com.mariaribeiro.nexo.identity.adapters.out.persistence.SpringDataUserRepository;
import com.mariaribeiro.nexo.identity.adapters.out.persistence.UserPersistenceAdapter;
import com.mariaribeiro.nexo.identity.adapters.out.security.AuthenticationRequestContext;
import com.mariaribeiro.nexo.identity.adapters.out.security.AuthTokenProperties;
import com.mariaribeiro.nexo.identity.adapters.out.security.BearerTokenAuthenticationFilter;
import com.mariaribeiro.nexo.identity.adapters.out.security.BcryptPasswordHashEncoder;
import com.mariaribeiro.nexo.identity.adapters.out.security.BcryptPasswordHashVerifier;
import com.mariaribeiro.nexo.identity.adapters.out.security.EmailVerificationProperties;
import com.mariaribeiro.nexo.identity.adapters.out.security.JwtTokenService;
import com.mariaribeiro.nexo.identity.adapters.out.security.PasswordResetProperties;
import com.mariaribeiro.nexo.identity.adapters.out.security.RefreshTokenProperties;
import com.mariaribeiro.nexo.identity.adapters.out.security.SecureRefreshTokenGenerator;
import com.mariaribeiro.nexo.identity.adapters.out.security.Sha256RefreshTokenHasher;
import java.time.Clock;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
    PasswordResetTokenPersistenceAdapter passwordResetTokenPersistenceAdapter(
            SpringDataPasswordResetTokenRepository passwordResetTokenRepository) {
        return new PasswordResetTokenPersistenceAdapter(passwordResetTokenRepository);
    }

    @Bean
    EmailVerificationTokenPersistenceAdapter emailVerificationTokenPersistenceAdapter(
            SpringDataEmailVerificationTokenRepository emailVerificationTokenRepository) {
        return new EmailVerificationTokenPersistenceAdapter(emailVerificationTokenRepository);
    }

    @Bean
    RefreshSessionPersistenceAdapter refreshSessionPersistenceAdapter(
            SpringDataRefreshSessionRepository refreshSessionRepository) {
        return new RefreshSessionPersistenceAdapter(refreshSessionRepository);
    }

    @Bean
    LoadUserByEmailPort loadUserByEmailPort(UserPersistenceAdapter userPersistenceAdapter) {
        return userPersistenceAdapter;
    }

    @Bean
    LoadUserByIdPort loadUserByIdPort(UserPersistenceAdapter userPersistenceAdapter) {
        return userPersistenceAdapter;
    }

    @Bean
    CreateUserPort createUserPort(UserPersistenceAdapter userPersistenceAdapter) {
        return userPersistenceAdapter;
    }

    @Bean
    UpdateUserPasswordPort updateUserPasswordPort(UserPersistenceAdapter userPersistenceAdapter) {
        return userPersistenceAdapter;
    }

    @Bean
    MarkUserEmailVerifiedPort markUserEmailVerifiedPort(UserPersistenceAdapter userPersistenceAdapter) {
        return userPersistenceAdapter;
    }

    @Bean
    CreatePasswordResetTokenPort createPasswordResetTokenPort(
            PasswordResetTokenPersistenceAdapter passwordResetTokenPersistenceAdapter) {
        return passwordResetTokenPersistenceAdapter;
    }

    @Bean
    CreateEmailVerificationTokenPort createEmailVerificationTokenPort(
            EmailVerificationTokenPersistenceAdapter emailVerificationTokenPersistenceAdapter) {
        return emailVerificationTokenPersistenceAdapter;
    }

    @Bean
    LoadPasswordResetTokenPort loadPasswordResetTokenPort(
            PasswordResetTokenPersistenceAdapter passwordResetTokenPersistenceAdapter) {
        return passwordResetTokenPersistenceAdapter;
    }

    @Bean
    DeletePasswordResetTokenPort deletePasswordResetTokenPort(
            PasswordResetTokenPersistenceAdapter passwordResetTokenPersistenceAdapter) {
        return passwordResetTokenPersistenceAdapter;
    }

    @Bean
    LoadEmailVerificationTokenPort loadEmailVerificationTokenPort(
            EmailVerificationTokenPersistenceAdapter emailVerificationTokenPersistenceAdapter) {
        return emailVerificationTokenPersistenceAdapter;
    }

    @Bean
    DeleteEmailVerificationTokenPort deleteEmailVerificationTokenPort(
            EmailVerificationTokenPersistenceAdapter emailVerificationTokenPersistenceAdapter) {
        return emailVerificationTokenPersistenceAdapter;
    }

    @Bean
    SaveRefreshSessionPort saveRefreshSessionPort(RefreshSessionPersistenceAdapter refreshSessionPersistenceAdapter) {
        return refreshSessionPersistenceAdapter;
    }

    @Bean
    LoadRefreshSessionByTokenHashPort loadRefreshSessionByTokenHashPort(
            RefreshSessionPersistenceAdapter refreshSessionPersistenceAdapter) {
        return refreshSessionPersistenceAdapter;
    }

    @Bean
    RevokeAllRefreshSessionsPort revokeAllRefreshSessionsPort(
            RefreshSessionPersistenceAdapter refreshSessionPersistenceAdapter) {
        return refreshSessionPersistenceAdapter;
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

    @Bean
    PasswordResetDeliveryPort passwordResetDeliveryPort() {
        return new StubPasswordResetDeliveryAdapter();
    }

    @Bean
    EmailVerificationDeliveryPort emailVerificationDeliveryPort() {
        return new StubEmailVerificationDeliveryAdapter();
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
            TokenServicePort tokenServicePort,
            RefreshSessionManager refreshSessionManager) {
        return new LoginService(loadUserByEmailPort, passwordHashVerifierPort, tokenServicePort, refreshSessionManager);
    }

    @Bean
    SignupUseCase signupUseCase(
            CreateUserPort createUserPort,
            PasswordHashEncoderPort passwordHashEncoderPort,
            TokenServicePort tokenServicePort,
            IssueEmailVerificationUseCase issueEmailVerificationUseCase,
            RefreshSessionManager refreshSessionManager,
            Clock authClock) {
        return new SignupService(
                createUserPort,
                passwordHashEncoderPort,
                tokenServicePort,
                issueEmailVerificationUseCase,
                refreshSessionManager,
                authClock);
    }

    @Bean
    RefreshSessionUseCase refreshSessionUseCase(
            RefreshSessionManager refreshSessionManager,
            LoadUserByIdPort loadUserByIdPort,
            TokenServicePort tokenServicePort) {
        return new RefreshSessionService(refreshSessionManager, loadUserByIdPort, tokenServicePort);
    }

    @Bean
    LogoutUseCase logoutUseCase(RefreshSessionManager refreshSessionManager) {
        return new LogoutService(refreshSessionManager);
    }

    @Bean
    LogoutAllUseCase logoutAllUseCase(RefreshSessionManager refreshSessionManager) {
        return new LogoutAllService(refreshSessionManager);
    }

    @Bean
    ForgotPasswordUseCase forgotPasswordUseCase(
            LoadUserByEmailPort loadUserByEmailPort,
            CreatePasswordResetTokenPort createPasswordResetTokenPort,
            PasswordResetDeliveryPort passwordResetDeliveryPort,
            Clock authClock,
            PasswordResetProperties passwordResetProperties) {
        return new ForgotPasswordService(
                loadUserByEmailPort,
                createPasswordResetTokenPort,
                passwordResetDeliveryPort,
                authClock,
                passwordResetProperties.getTokenTtl());
    }

    @Bean
    ResetPasswordUseCase resetPasswordUseCase(
            LoadPasswordResetTokenPort loadPasswordResetTokenPort,
            UpdateUserPasswordPort updateUserPasswordPort,
            DeletePasswordResetTokenPort deletePasswordResetTokenPort,
            PasswordHashEncoderPort passwordHashEncoderPort,
            Clock authClock) {
        return new ResetPasswordService(
                loadPasswordResetTokenPort,
                updateUserPasswordPort,
                deletePasswordResetTokenPort,
                passwordHashEncoderPort,
                authClock);
    }

    @Bean
    IssueEmailVerificationUseCase issueEmailVerificationUseCase(
            CreateEmailVerificationTokenPort createEmailVerificationTokenPort,
            DeleteEmailVerificationTokenPort deleteEmailVerificationTokenPort,
            EmailVerificationDeliveryPort emailVerificationDeliveryPort,
            Clock authClock,
            EmailVerificationProperties emailVerificationProperties) {
        return new IssueEmailVerificationService(
                createEmailVerificationTokenPort,
                deleteEmailVerificationTokenPort,
                emailVerificationDeliveryPort,
                authClock,
                emailVerificationProperties.getTokenTtl());
    }

    @Bean
    ResendVerificationEmailUseCase resendVerificationEmailUseCase(
            LoadUserByEmailPort loadUserByEmailPort,
            IssueEmailVerificationUseCase issueEmailVerificationUseCase) {
        return new ResendVerificationEmailService(loadUserByEmailPort, issueEmailVerificationUseCase);
    }

    @Bean
    VerifyEmailUseCase verifyEmailUseCase(
            LoadEmailVerificationTokenPort loadEmailVerificationTokenPort,
            MarkUserEmailVerifiedPort markUserEmailVerifiedPort,
            DeleteEmailVerificationTokenPort deleteEmailVerificationTokenPort,
            Clock authClock) {
        return new VerifyEmailService(
                loadEmailVerificationTokenPort,
                markUserEmailVerifiedPort,
                deleteEmailVerificationTokenPort,
                authClock);
    }
}
