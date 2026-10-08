package com.example.objectverse.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class KnowledgePracticeMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public KnowledgePracticeMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        executeIgnoringFailure("""
                CREATE TABLE IF NOT EXISTS knowledge_note (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id BIGINT NOT NULL,
                    title VARCHAR(128) NOT NULL,
                    category VARCHAR(64),
                    content TEXT NOT NULL,
                    source_type VARCHAR(64) NOT NULL DEFAULT 'CODE_REVIEW',
                    create_time DATETIME,
                    update_time DATETIME,
                    deleted TINYINT DEFAULT 0,
                    INDEX idx_knowledge_note_user_id (user_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);
        executeIgnoringFailure("""
                CREATE TABLE IF NOT EXISTS practice_question (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id BIGINT NOT NULL,
                    knowledge_note_id BIGINT,
                    question_type VARCHAR(32) NOT NULL,
                    title VARCHAR(255) NOT NULL,
                    body TEXT,
                    answer TEXT,
                    create_time DATETIME,
                    update_time DATETIME,
                    deleted TINYINT DEFAULT 0,
                    INDEX idx_practice_question_user_id (user_id),
                    INDEX idx_practice_question_note_id (knowledge_note_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);
    }

    private void executeIgnoringFailure(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (DataAccessException ignored) {
            // Local databases may already have equivalent objects from previous runs.
        }
    }
}
