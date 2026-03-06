package com.mariaribeiro.nexo.identity.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mariaribeiro.nexo.identity.application.port.TokenServicePort;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AuthControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenServicePort tokenServicePort;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void cleanUsers() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM email_verification_tokens");
            statement.executeUpdate("DELETE FROM password_reset_tokens");
            statement.executeUpdate("DELETE FROM users");
        }
    }

    @Test
    void loginReturnsAccessTokenForValidCredentials() throws Exception {
        insertUser("person@example.com", passwordEncoder.encode("secret123"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/auth/login"))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {"email":"Person@Example.com","password":"secret123"}
                        """))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(responseBody).containsKeys("accessToken", "expiresAt");
        assertThat(responseBody.get("accessToken")).isInstanceOf(String.class);
        assertThat(tokenServicePort.verify((String) responseBody.get("accessToken")).email())
                .isEqualTo("person@example.com");
        assertThat(Instant.parse((String) responseBody.get("expiresAt")))
                .isAfter(Instant.now().minusSeconds(1));
    }

    @Test
    void loginReturnsGenericUnauthorizedForUnknownEmail() throws Exception {
        HttpResponse<String> response = sendLogin("missing@example.com", "secret123");
        Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(responseBody).containsEntry("message", "Invalid credentials");
    }

    @Test
    void loginReturnsGenericUnauthorizedForWrongPassword() throws Exception {
        insertUser("person@example.com", passwordEncoder.encode("secret123"));

        HttpResponse<String> response = sendLogin("person@example.com", "wrong-password");
        Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(responseBody).containsEntry("message", "Invalid credentials");
    }

    @Test
    void signupCreatesNormalizedUserAndReturnsAccessToken() throws Exception {
        HttpResponse<String> response = sendSignup("Person@Example.com", "secret123");
        Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
        Map<String, Object> storedUser = loadUserByEmail("person@example.com");

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(responseBody).containsKeys("accessToken", "expiresAt");
        assertThat(tokenServicePort.verify((String) responseBody.get("accessToken")).email())
                .isEqualTo("person@example.com");
        assertThat(Instant.parse((String) responseBody.get("expiresAt")))
                .isAfter(Instant.now().minusSeconds(1));
        assertThat(storedUser).containsEntry("email", "person@example.com");
        assertThat(storedUser.get("password_hash")).isInstanceOf(String.class);
        assertThat(storedUser.get("password_hash")).isNotEqualTo("secret123");
        assertThat(passwordEncoder.matches("secret123", (String) storedUser.get("password_hash"))).isTrue();
        assertThat(storedUser).containsEntry("email_verified", false);
        assertThat(storedUser.get("created_at")).isNotNull();
        assertThat(countEmailVerificationTokens()).isEqualTo(1);
    }

    @Test
    void signupReturnsConflictWhenEmailAlreadyExists() throws Exception {
        insertUser("person@example.com", passwordEncoder.encode("secret123"));

        HttpResponse<String> response = sendSignup("Person@Example.com", "secret123");
        Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);

        assertThat(response.statusCode()).isEqualTo(409);
        assertThat(responseBody).containsEntry("message", "Unable to create account");
    }

    @Test
    void signupReturnsFieldValidationErrorsForInvalidPayload() throws Exception {
        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/auth/signup"))
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .POST(HttpRequest.BodyPublishers.ofString("""
                                {"email":"invalid-email","password":"short"}
                                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
        Map<String, Object> errors = (Map<String, Object>) responseBody.get("errors");

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(responseBody).containsEntry("message", "Validation failed");
        assertThat(errors).containsEntry("email", "Enter a valid email address");
        assertThat(errors).containsEntry("password", "Password must be at least 8 characters");
    }

    @Test
    void forgotPasswordReturnsGenericAcceptedResponseAndPersistsExpiringTokenForExistingUser() throws Exception {
        insertUser("person@example.com", passwordEncoder.encode("secret123"));

        HttpResponse<String> response = sendForgotPassword("Person@Example.com");
        Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
        Map<String, Object> storedToken = loadPasswordResetToken();

        assertThat(response.statusCode()).isEqualTo(202);
        assertThat(responseBody).containsEntry("message", "Check your email");
        assertThat(storedToken.get("token")).isInstanceOf(String.class);
        assertThat(storedToken.get("token")).isNotEqualTo("");
        assertThat(Instant.parse((String) storedToken.get("expires_at")))
                .isAfter(Instant.parse((String) storedToken.get("created_at")));
    }

    @Test
    void forgotPasswordReturnsSameGenericAcceptedResponseWhenUserDoesNotExist() throws Exception {
        HttpResponse<String> response = sendForgotPassword("missing@example.com");
        Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);

        assertThat(response.statusCode()).isEqualTo(202);
        assertThat(responseBody).containsEntry("message", "Check your email");
        assertThat(countPasswordResetTokens()).isZero();
    }

    @Test
    void forgotPasswordReturnsFieldValidationErrorsForInvalidPayload() throws Exception {
        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/auth/forgot-password"))
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .POST(HttpRequest.BodyPublishers.ofString("""
                                {"email":"invalid-email"}
                                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
        Map<String, Object> errors = (Map<String, Object>) responseBody.get("errors");

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(responseBody).containsEntry("message", "Validation failed");
        assertThat(errors).containsEntry("email", "Enter a valid email address");
    }

    @Test
    void verifyEmailMarksUserVerifiedAndInvalidatesToken() throws Exception {
        insertUser("person@example.com", passwordEncoder.encode("secret123"));
        Map<String, Object> storedUser = loadUserByEmail("person@example.com");
        insertEmailVerificationToken(
                UUID.randomUUID(),
                (UUID) storedUser.get("id"),
                "verify-token",
                Instant.parse("2026-03-07T12:00:00Z"),
                Instant.parse("2026-03-06T12:00:00Z"));

        HttpResponse<String> response = sendVerifyEmail("verify-token");
        Map<String, Object> updatedUser = loadUserByEmail("person@example.com");

        assertThat(response.statusCode()).isEqualTo(204);
        assertThat(updatedUser).containsEntry("email_verified", true);
        assertThat(updatedUser.get("email_verified_at")).isNotNull();
        assertThat(countEmailVerificationTokens()).isZero();
    }

    @Test
    void verifyEmailRejectsExpiredTokenWithGenericMessage() throws Exception {
        insertUser("person@example.com", passwordEncoder.encode("secret123"));
        Map<String, Object> storedUser = loadUserByEmail("person@example.com");
        insertEmailVerificationToken(
                UUID.randomUUID(),
                (UUID) storedUser.get("id"),
                "expired-verify-token",
                Instant.parse("2026-03-06T11:00:00Z"),
                Instant.parse("2026-03-06T10:00:00Z"));

        HttpResponse<String> response = sendVerifyEmail("expired-verify-token");
        Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(responseBody).containsEntry("message", "Invalid verification token");
        assertThat(countEmailVerificationTokens()).isEqualTo(1);
    }

    @Test
    void verifyEmailReturnsFieldValidationErrorsForInvalidPayload() throws Exception {
        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/auth/verify-email"))
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .POST(HttpRequest.BodyPublishers.ofString("""
                                {"token":""}
                                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
        Map<String, Object> errors = (Map<String, Object>) responseBody.get("errors");

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(responseBody).containsEntry("message", "Validation failed");
        assertThat(errors).containsEntry("token", "Verification token is required");
    }

    @Test
    void resetPasswordUpdatesStoredHashInvalidatesTokenAndAllowsLoginWithNewPassword() throws Exception {
        insertUser("person@example.com", passwordEncoder.encode("secret123"));
        Map<String, Object> storedUser = loadUserByEmail("person@example.com");
        insertPasswordResetToken(
                UUID.randomUUID(),
                (UUID) storedUser.get("id"),
                "valid-reset-token",
                Instant.parse("2026-03-07T12:15:00Z"),
                Instant.parse("2026-03-06T12:00:00Z"));

        HttpResponse<String> resetResponse = sendResetPassword("valid-reset-token", "new-secret123");
        HttpResponse<String> oldLoginResponse = sendLogin("person@example.com", "secret123");
        HttpResponse<String> newLoginResponse = sendLogin("person@example.com", "new-secret123");
        Map<String, Object> oldLoginBody = objectMapper.readValue(oldLoginResponse.body(), Map.class);
        Map<String, Object> newLoginBody = objectMapper.readValue(newLoginResponse.body(), Map.class);
        Map<String, Object> updatedUser = loadUserByEmail("person@example.com");

        assertThat(resetResponse.statusCode()).isEqualTo(204);
        assertThat(updatedUser.get("password_hash")).isInstanceOf(String.class);
        assertThat(updatedUser.get("password_hash")).isNotEqualTo(storedUser.get("password_hash"));
        assertThat(passwordEncoder.matches("new-secret123", (String) updatedUser.get("password_hash"))).isTrue();
        assertThat(countPasswordResetTokens()).isZero();
        assertThat(oldLoginResponse.statusCode()).isEqualTo(401);
        assertThat(oldLoginBody).containsEntry("message", "Invalid credentials");
        assertThat(newLoginResponse.statusCode()).isEqualTo(200);
        assertThat(newLoginBody).containsKeys("accessToken", "expiresAt");
    }

    @Test
    void resetPasswordRejectsTokenReuse() throws Exception {
        insertUser("person@example.com", passwordEncoder.encode("secret123"));
        Map<String, Object> storedUser = loadUserByEmail("person@example.com");
        insertPasswordResetToken(
                UUID.randomUUID(),
                (UUID) storedUser.get("id"),
                "single-use-token",
                Instant.parse("2026-03-07T12:15:00Z"),
                Instant.parse("2026-03-06T12:00:00Z"));

        HttpResponse<String> firstResponse = sendResetPassword("single-use-token", "new-secret123");
        HttpResponse<String> secondResponse = sendResetPassword("single-use-token", "another-secret123");
        Map<String, Object> secondBody = objectMapper.readValue(secondResponse.body(), Map.class);

        assertThat(firstResponse.statusCode()).isEqualTo(204);
        assertThat(secondResponse.statusCode()).isEqualTo(400);
        assertThat(secondBody).containsEntry("message", "Invalid reset token");
    }

    @Test
    void resetPasswordRejectsExpiredTokenWithGenericMessage() throws Exception {
        insertUser("person@example.com", passwordEncoder.encode("secret123"));
        Map<String, Object> storedUser = loadUserByEmail("person@example.com");
        insertPasswordResetToken(
                UUID.randomUUID(),
                (UUID) storedUser.get("id"),
                "expired-reset-token",
                Instant.parse("2026-03-06T11:59:59Z"),
                Instant.parse("2026-03-06T11:45:00Z"));

        HttpResponse<String> response = sendResetPassword("expired-reset-token", "new-secret123");
        Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(responseBody).containsEntry("message", "Invalid reset token");
        assertThat(countPasswordResetTokens()).isEqualTo(1);
    }

    @Test
    void resetPasswordReturnsFieldValidationErrorsForInvalidPayload() throws Exception {
        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/auth/reset-password"))
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .POST(HttpRequest.BodyPublishers.ofString("""
                                {"token":"","newPassword":"short"}
                                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
        Map<String, Object> errors = (Map<String, Object>) responseBody.get("errors");

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(responseBody).containsEntry("message", "Validation failed");
        assertThat(errors).containsEntry("token", "Reset token is required");
        assertThat(errors).containsEntry("newPassword", "Password must be at least 8 characters");
    }

    private HttpResponse<String> sendLogin(String email, String password) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/auth/login"))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(
                        objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
                .build();

        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendSignup(String email, String password) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/auth/signup"))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(
                        objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
                .build();

        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendForgotPassword(String email) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/auth/forgot-password"))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(
                        objectMapper.writeValueAsString(Map.of("email", email))))
                .build();

        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendResetPassword(String token, String newPassword) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/auth/reset-password"))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(
                        objectMapper.writeValueAsString(Map.of("token", token, "newPassword", newPassword))))
                .build();

        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendVerifyEmail(String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/auth/verify-email?token=" + token))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private Map<String, Object> loadUserByEmail(String email) throws Exception {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     """
                     SELECT id, email, password_hash, email_verified, email_verified_at, created_at
                     FROM users WHERE email = ?
                     """)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                Map<String, Object> user = new HashMap<>();
                user.put("id", resultSet.getObject("id"));
                user.put("email", resultSet.getString("email"));
                user.put("password_hash", resultSet.getString("password_hash"));
                user.put("email_verified", resultSet.getBoolean("email_verified"));
                user.put("email_verified_at", resultSet.getTimestamp("email_verified_at"));
                user.put("created_at", resultSet.getTimestamp("created_at"));
                return user;
            }
        }
    }

    private Map<String, Object> loadPasswordResetToken() throws Exception {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     """
                     SELECT token, expires_at, created_at
                     FROM password_reset_tokens
                     FETCH FIRST 1 ROWS ONLY
                     """)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                Map<String, Object> token = new HashMap<>();
                token.put("token", resultSet.getString("token"));
                token.put("expires_at", resultSet.getTimestamp("expires_at").toInstant().toString());
                token.put("created_at", resultSet.getTimestamp("created_at").toInstant().toString());
                return token;
            }
        }
    }

    private int countPasswordResetTokens() throws Exception {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM password_reset_tokens");
             ResultSet resultSet = statement.executeQuery()) {
            assertThat(resultSet.next()).isTrue();
            return resultSet.getInt(1);
        }
    }

    private int countEmailVerificationTokens() throws Exception {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM email_verification_tokens");
             ResultSet resultSet = statement.executeQuery()) {
            assertThat(resultSet.next()).isTrue();
            return resultSet.getInt(1);
        }
    }

    private void insertPasswordResetToken(
            UUID id,
            UUID userId,
            String token,
            Instant expiresAt,
            Instant createdAt) throws Exception {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     """
                     INSERT INTO password_reset_tokens (id, user_id, token, expires_at, created_at)
                     VALUES (?, ?, ?, ?, ?)
                     """)) {
            statement.setObject(1, id);
            statement.setObject(2, userId);
            statement.setString(3, token);
            statement.setTimestamp(4, Timestamp.from(expiresAt));
            statement.setTimestamp(5, Timestamp.from(createdAt));
            statement.executeUpdate();
        }
    }

    private void insertEmailVerificationToken(
            UUID id,
            UUID userId,
            String token,
            Instant expiresAt,
            Instant createdAt) throws Exception {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     """
                     INSERT INTO email_verification_tokens (id, user_id, token, expires_at, created_at)
                     VALUES (?, ?, ?, ?, ?)
                     """)) {
            statement.setObject(1, id);
            statement.setObject(2, userId);
            statement.setString(3, token);
            statement.setTimestamp(4, Timestamp.from(expiresAt));
            statement.setTimestamp(5, Timestamp.from(createdAt));
            statement.executeUpdate();
        }
    }

    private void insertUser(String email, String passwordHash) throws Exception {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO users (id, email, password_hash, created_at) VALUES (?, ?, ?, ?)")) {
            statement.setObject(1, UUID.randomUUID());
            statement.setString(2, email);
            statement.setString(3, passwordHash);
            statement.setTimestamp(4, Timestamp.from(Instant.parse("2026-03-06T12:00:00Z")));
            statement.executeUpdate();
        }
    }
}
