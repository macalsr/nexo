package com.mariaribeiro.nexo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NexoApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Test
    void contextLoads() {
    }

    @Test
    void flywayMigrationCreatesAppMetadataTable() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (var tables = metaData.getTables(null, null, "app_metadata", null)) {
                assertThat(tables.next()).isTrue();
            }
        }
    }

    @Test
    void flywayMigrationCreatesUsersTable() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (var tables = metaData.getTables(null, null, "users", null)) {
                assertThat(tables.next()).isTrue();
            }
        }
    }

    @Test
    void usersTableEnforcesLowercaseEmailUniqueness() throws Exception {
        try (Connection connection = dataSource.getConnection();
             var firstInsert = connection.prepareStatement(
                     "INSERT INTO users (id, email, password_hash) VALUES (RANDOM_UUID(), ?, ?)");
             var secondInsert = connection.prepareStatement(
                     "INSERT INTO users (id, email, password_hash) VALUES (RANDOM_UUID(), ?, ?)")) {

            firstInsert.setString(1, "person@example.com");
            firstInsert.setString(2, "$2a$10$abcdefghijklmnopqrstuv");
            firstInsert.executeUpdate();

            secondInsert.setString(1, "person@example.com");
            secondInsert.setString(2, "$2a$10$zyxwvutsrqponmlkjihgfe");

            assertThatThrownBy(secondInsert::executeUpdate)
                    .isInstanceOf(SQLException.class);
        }
    }

    @Test
    void usersTableRejectsNonNormalizedEmailValues() throws Exception {
        try (Connection connection = dataSource.getConnection();
             var insert = connection.prepareStatement(
                     "INSERT INTO users (id, email, password_hash) VALUES (RANDOM_UUID(), ?, ?)")) {

            insert.setString(1, "Person@Example.com");
            insert.setString(2, "$2a$10$abcdefghijklmnopqrstuv");

            assertThatThrownBy(insert::executeUpdate)
                    .isInstanceOf(SQLException.class);
        }
    }

}
