package com.mariaribeiro.nexo.workspace.adapters.in.rest;

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
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class WorkspaceControllerTest {

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
    void cleanBefore() throws Exception {
        cleanTables();
    }

    @AfterEach
    void cleanAfter() throws Exception {
        cleanTables();
    }

    @Test
    void createWorkspaceReturnsUnauthorizedWhenTokenIsMissing() throws Exception {
        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/workspaces"))
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .POST(HttpRequest.BodyPublishers.ofString("""
                                {"name":"Personal"}
                                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(responseBody).containsEntry("message", "Unauthorized");
    }

    @Test
    void createWorkspaceReturnsValidationErrorWhenNameIsBlank() throws Exception {
        String accessToken = loginAndGetAccessToken("person@example.com", "secret123");

        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/workspaces"))
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .POST(HttpRequest.BodyPublishers.ofString("""
                                {"name":"   "}
                                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
        Map<String, Object> errors = (Map<String, Object>) responseBody.get("errors");

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(responseBody).containsEntry("message", "Validation failed");
        assertThat(errors).containsEntry("name", "Name is required");
    }

    @Test
    void createWorkspaceReturnsValidationErrorWhenNameIsTooLong() throws Exception {
        String accessToken = loginAndGetAccessToken("person@example.com", "secret123");
        String longName = "a".repeat(121);

        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/workspaces"))
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .POST(HttpRequest.BodyPublishers.ofString(
                                objectMapper.writeValueAsString(Map.of("name", longName))))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
        Map<String, Object> errors = (Map<String, Object>) responseBody.get("errors");

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(responseBody).containsEntry("message", "Validation failed");
        assertThat(errors).containsEntry("name", "Name must be at most 120 characters");
    }

    @Test
    void createWorkspaceCreatesWorkspaceAndOwnerMembership() throws Exception {
        String accessToken = loginAndGetAccessToken("person@example.com", "secret123");
        UUID userId = tokenServicePort.verify(accessToken).userId();

        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/workspaces"))
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .POST(HttpRequest.BodyPublishers.ofString("""
                                {"name":"Personal"}
                                """))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
        assertThat(response.statusCode()).isEqualTo(201);
        UUID workspaceId = UUID.fromString((String) responseBody.get("id"));

        assertThat(responseBody).containsEntry("name", "Personal");
        assertWorkspaceExists(workspaceId, "Personal", userId);
        assertWorkspaceMembershipExists(workspaceId, userId, "OWNER");
    }

    private String loginAndGetAccessToken(String email, String password) throws Exception {
        insertUser(email, passwordEncoder.encode(password));
        HttpResponse<String> loginResponse = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + "/auth/login"))
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .POST(HttpRequest.BodyPublishers.ofString(
                                objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        Map<String, Object> loginBody = objectMapper.readValue(loginResponse.body(), Map.class);
        return (String) loginBody.get("accessToken");
    }

    private void assertWorkspaceExists(UUID workspaceId, String expectedName, UUID expectedCreatedBy) throws Exception {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT name, created_by FROM workspaces WHERE id = ?")) {
            statement.setObject(1, workspaceId);
            try (ResultSet resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getString("name")).isEqualTo(expectedName);
                assertThat(resultSet.getObject("created_by", UUID.class)).isEqualTo(expectedCreatedBy);
            }
        }
    }

    private void assertWorkspaceMembershipExists(UUID workspaceId, UUID userId, String expectedRole) throws Exception {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT role FROM workspace_members WHERE workspace_id = ? AND user_id = ?")) {
            statement.setObject(1, workspaceId);
            statement.setObject(2, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getString("role")).isEqualTo(expectedRole);
            }
        }
    }

    private void cleanTables() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM workspace_members");
            statement.executeUpdate("DELETE FROM workspaces");
            statement.executeUpdate("DELETE FROM refresh_sessions");
            statement.executeUpdate("DELETE FROM email_verification_tokens");
            statement.executeUpdate("DELETE FROM password_reset_tokens");
            statement.executeUpdate("DELETE FROM users");
        }
    }

    private void insertUser(String email, String passwordHash) throws Exception {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO users (id, email, password_hash, created_at) VALUES (?, ?, ?, ?)")) {
            statement.setObject(1, UUID.randomUUID());
            statement.setString(2, email);
            statement.setString(3, passwordHash);
            statement.setTimestamp(4, Timestamp.from(Instant.parse("2026-03-07T12:00:00Z")));
            statement.executeUpdate();
        }
    }
}
