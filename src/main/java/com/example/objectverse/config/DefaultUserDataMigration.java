package com.example.objectverse.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DefaultUserDataMigration implements CommandLineRunner {

    private static final long LEGACY_DEFAULT_USER_ID = 1L;
    private static final String MIGRATION_USERNAME = "12345";
    private static final String MIGRATION_PASSWORD = "12345";

    private final JdbcTemplate jdbcTemplate;

    public DefaultUserDataMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        Long targetUserId = ensureMigrationUser();
        if (targetUserId == null || targetUserId == LEGACY_DEFAULT_USER_ID) {
            return;
        }
        moveLegacyUserData(targetUserId);
    }

    private Long ensureMigrationUser() {
        Long existingId = findUserId(MIGRATION_USERNAME);
        if (existingId != null) {
            jdbcTemplate.update(
                    "UPDATE user_account SET password = ?, nickname = ?, status = 'ACTIVE', deleted = 0, update_time = NOW() WHERE id = ?",
                    MIGRATION_PASSWORD,
                    MIGRATION_USERNAME,
                    existingId
            );
            return existingId;
        }
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update("""
                INSERT INTO user_account (
                    username,
                    password,
                    nickname,
                    role,
                    status,
                    create_time,
                    update_time,
                    deleted
                ) VALUES (?, ?, ?, 'STUDENT', 'ACTIVE', ?, ?, 0)
                """, MIGRATION_USERNAME, MIGRATION_PASSWORD, MIGRATION_USERNAME, now, now);
        return findUserId(MIGRATION_USERNAME);
    }

    private Long findUserId(String username) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id FROM user_account WHERE username = ? LIMIT 1",
                    Long.class,
                    username
            );
        } catch (DataAccessException ex) {
            return null;
        }
    }

    private void moveLegacyUserData(Long targetUserId) {
        updateUserReference("project", targetUserId);
        updateUserReference("knowledge_note", targetUserId);
        updateUserReference("knowledge_note_reaction", targetUserId);
        updateUserReference("practice_question", targetUserId);
    }

    private void updateUserReference(String tableName, Long targetUserId) {
        try {
            jdbcTemplate.update(
                    "UPDATE " + tableName + " SET user_id = ? WHERE user_id = ?",
                    targetUserId,
                    LEGACY_DEFAULT_USER_ID
            );
        } catch (DataAccessException ignored) {
            // Some local databases may not have every optional table yet.
        }
    }
}
