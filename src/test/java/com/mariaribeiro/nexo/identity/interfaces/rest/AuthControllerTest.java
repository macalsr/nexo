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
import java.sql.Statement;
import java.time.Instant;
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

    private HttpResponse<String> sendLogin(String email, String password) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/auth/login"))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(
                        objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
                .build();

        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void insertUser(String email, String passwordHash) throws Exception {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO users (id, email, password_hash, created_at) VALUES (?, ?, ?, ?)")) {
            statement.setObject(1, UUID.randomUUID());
            statement.setString(2, email);
            statement.setString(3, passwordHash);
            statement.setObject(4, Instant.parse("2026-03-06T12:00:00Z"));
            statement.executeUpdate();
        }
    }
}
