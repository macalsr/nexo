package com.mariaribeiro.nexo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.UUID;
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
                     "INSERT INTO users (id, email, password_hash) VALUES (?, ?, ?)");
             var secondInsert = connection.prepareStatement(
                     "INSERT INTO users (id, email, password_hash) VALUES (?, ?, ?)")) {

            firstInsert.setObject(1, UUID.randomUUID());
            firstInsert.setString(2, "person@example.com");
            firstInsert.setString(3, "$2a$10$abcdefghijklmnopqrstuv");
            firstInsert.executeUpdate();

            secondInsert.setObject(1, UUID.randomUUID());
            secondInsert.setString(2, "person@example.com");
            secondInsert.setString(3, "$2a$10$zyxwvutsrqponmlkjihgfe");

            assertThatThrownBy(secondInsert::executeUpdate)
                    .isInstanceOf(SQLException.class);
        }
    }

    @Test
    void usersTableRejectsNonNormalizedEmailValues() throws Exception {
        try (Connection connection = dataSource.getConnection();
             var insert = connection.prepareStatement(
                     "INSERT INTO users (id, email, password_hash) VALUES (?, ?, ?)")) {

            insert.setObject(1, UUID.randomUUID());
            insert.setString(2, "Person@Example.com");
            insert.setString(3, "$2a$10$abcdefghijklmnopqrstuv");

            assertThatThrownBy(insert::executeUpdate)
                    .isInstanceOf(SQLException.class);
        }
    }

}

