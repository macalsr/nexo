package com.mariaribeiro.nexo.identity.adapters.in.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mariaribeiro.nexo.identity.application.port.TokenServicePort;
import com.mariaribeiro.nexo.identity.adapters.out.security.AuthTokenProperties;
import com.mariaribeiro.nexo.identity.adapters.out.security.JwtTokenService;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AuthenticatedUserControllerTest {

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
            statement.executeUpdate("DELETE FROM users");
        }
    }

    @Test
    void meReturnsUnauthorizedWhenTokenIsMissing() throws Exception {
        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/me"))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(responseBody).containsEntry("message", "Unauthorized");
    }

    @Test
    void meReturnsUnauthorizedWhenTokenIsInvalid() throws Exception {
        HttpResponse<String> response = sendProtectedRequest("Bearer invalid-token");
        Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(responseBody).containsEntry("message", "Unauthorized");
    }

    @Test
    void meReturnsUnauthorizedWhenTokenIsExpired() throws Exception {
        AuthTokenProperties properties = new AuthTokenProperties();
        properties.setSecret("change-this-local-dev-jwt-secret-key-1234567890");
        properties.setAccessTokenTtl(Duration.ofHours(24));
        JwtTokenService expiredTokenService = new JwtTokenService(
                Clock.fixed(Instant.parse("2026-03-01T12:00:00Z"), ZoneOffset.UTC),
                properties);

        HttpResponse<String> response = sendProtectedRequest(
                "Bearer " + expiredTokenService.issueToken(UUID.randomUUID(), "person@example.com").value());
        Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(responseBody).containsEntry("message", "Unauthorized");
    }

    @Test
    void meReturnsAuthenticatedUserWhenTokenIsValid() throws Exception {
        insertUser("person@example.com", passwordEncoder.encode("secret123"));
        HttpResponse<String> loginResponse = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/auth/login"))
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .POST(HttpRequest.BodyPublishers.ofString("""
                                {"email":"person@example.com","password":"secret123"}
                                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        String accessToken = (String) objectMapper.readValue(loginResponse.body(), Map.class).get("accessToken");

        HttpResponse<String> response = sendProtectedRequest("Bearer " + accessToken);
        Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(responseBody).containsEntry("email", "person@example.com");
        assertThat(responseBody).containsEntry("emailVerified", false);
        assertThat(responseBody.get("userId")).isNotNull();
    }

    @Test
    void meReturnsEmailVerifiedStateWhenUserIsVerified() throws Exception {
        insertVerifiedUser("person@example.com", passwordEncoder.encode("secret123"));
        HttpResponse<String> loginResponse = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/auth/login"))
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .POST(HttpRequest.BodyPublishers.ofString("""
                                {"email":"person@example.com","password":"secret123"}
                                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        String accessToken = (String) objectMapper.readValue(loginResponse.body(), Map.class).get("accessToken");

        HttpResponse<String> response = sendProtectedRequest("Bearer " + accessToken);
        Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(responseBody).containsEntry("emailVerified", true);
    }

    @Test
    void resendVerificationReturnsGenericAcceptedResponseForAuthenticatedUser() throws Exception {
        insertUser("person@example.com", passwordEncoder.encode("secret123"));
        HttpResponse<String> loginResponse = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/auth/login"))
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .POST(HttpRequest.BodyPublishers.ofString("""
                                {"email":"person@example.com","password":"secret123"}
                                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        String accessToken = (String) objectMapper.readValue(loginResponse.body(), Map.class).get("accessToken");

        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/auth/resend-verification"))
                        .header("Authorization", "Bearer " + accessToken)
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);

        assertThat(response.statusCode()).isEqualTo(202);
        assertThat(responseBody).containsEntry("message", "Check your email");
    }

    @Test
    void meAllowsCorsPreflightRequestsWithoutAuthentication() throws Exception {
        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/me"))
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization,content-type")
                        .method(HttpMethod.OPTIONS.name(), HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("access-control-allow-origin"))
                .contains("http://localhost:5173");
        assertThat(response.headers().firstValue("access-control-allow-methods").orElse(""))
                .contains("GET");
    }

    private HttpResponse<String> sendProtectedRequest(String authorizationHeader) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/me"))
                .GET();

        if (authorizationHeader != null) {
            requestBuilder.header("Authorization", authorizationHeader);
        }

        return HttpClient.newHttpClient().send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
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

    private void insertVerifiedUser(String email, String passwordHash) throws Exception {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     """
                     INSERT INTO users (id, email, password_hash, email_verified, email_verified_at, created_at)
                     VALUES (?, ?, ?, ?, ?, ?)
                     """)) {
            statement.setObject(1, UUID.randomUUID());
            statement.setString(2, email);
            statement.setString(3, passwordHash);
            statement.setBoolean(4, true);
            statement.setTimestamp(5, Timestamp.from(Instant.parse("2026-03-06T12:30:00Z")));
            statement.setTimestamp(6, Timestamp.from(Instant.parse("2026-03-06T12:00:00Z")));
            statement.executeUpdate();
        }
    }
}
