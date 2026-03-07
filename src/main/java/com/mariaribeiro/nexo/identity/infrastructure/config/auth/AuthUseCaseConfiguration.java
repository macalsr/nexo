package com.mariaribeiro.nexo.identity.infrastructure.config.auth;

import com.mariaribeiro.nexo.identity.adapters.out.notification.StubEmailVerificationDeliveryAdapter;
import com.mariaribeiro.nexo.identity.adapters.out.notification.StubPasswordResetDeliveryAdapter;
import com.mariaribeiro.nexo.identity.adapters.out.security.EmailVerificationProperties;
import com.mariaribeiro.nexo.identity.adapters.out.security.PasswordResetProperties;
import com.mariaribeiro.nexo.identity.adapters.out.security.RefreshTokenProperties;
import com.mariaribeiro.nexo.identity.application.auth.LoginService;
import com.mariaribeiro.nexo.identity.application.auth.LoginUseCase;
import com.mariaribeiro.nexo.identity.application.auth.LogoutAllService;
import com.mariaribeiro.nexo.identity.application.auth.LogoutAllUseCase;
import com.mariaribeiro.nexo.identity.application.auth.LogoutService;
import com.mariaribeiro.nexo.identity.application.auth.LogoutUseCase;
import com.mariaribeiro.nexo.identity.application.auth.RefreshSessionManager;
import com.mariaribeiro.nexo.identity.application.auth.RefreshSessionService;
import com.mariaribeiro.nexo.identity.application.auth.RefreshSessionUseCase;
import com.mariaribeiro.nexo.identity.application.auth.SignupService;
import com.mariaribeiro.nexo.identity.application.auth.SignupUseCase;
import com.mariaribeiro.nexo.identity.application.port.CreateEmailVerificationTokenPort;
import com.mariaribeiro.nexo.identity.application.port.CreatePasswordResetTokenPort;
import com.mariaribeiro.nexo.identity.application.port.CreateUserPort;
import com.mariaribeiro.nexo.identity.application.port.DeleteEmailVerificationTokenPort;
import com.mariaribeiro.nexo.identity.application.port.DeletePasswordResetTokenPort;
import com.mariaribeiro.nexo.identity.application.port.EmailVerificationDeliveryPort;
import com.mariaribeiro.nexo.identity.application.port.LoadEmailVerificationTokenPort;
import com.mariaribeiro.nexo.identity.application.port.LoadPasswordResetTokenPort;
import com.mariaribeiro.nexo.identity.application.port.LoadRefreshSessionByTokenHashPort;
import com.mariaribeiro.nexo.identity.application.port.LoadUserByEmailPort;
import com.mariaribeiro.nexo.identity.application.port.LoadUserByIdPort;
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
import com.mariaribeiro.nexo.identity.application.recovery.ForgotPasswordService;
import com.mariaribeiro.nexo.identity.application.recovery.ForgotPasswordUseCase;
import com.mariaribeiro.nexo.identity.application.recovery.ResetPasswordService;
import com.mariaribeiro.nexo.identity.application.recovery.ResetPasswordUseCase;
import com.mariaribeiro.nexo.identity.application.verification.IssueEmailVerificationService;
import com.mariaribeiro.nexo.identity.application.verification.IssueEmailVerificationUseCase;
import com.mariaribeiro.nexo.identity.application.verification.ResendVerificationEmailService;
import com.mariaribeiro.nexo.identity.application.verification.ResendVerificationEmailUseCase;
import com.mariaribeiro.nexo.identity.application.verification.VerifyEmailService;
import com.mariaribeiro.nexo.identity.application.verification.VerifyEmailUseCase;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthUseCaseConfiguration {

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

    @Bean
    PasswordResetDeliveryPort passwordResetDeliveryPort() {
        return new StubPasswordResetDeliveryAdapter();
    }

    @Bean
    EmailVerificationDeliveryPort emailVerificationDeliveryPort() {
        return new StubEmailVerificationDeliveryAdapter();
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
