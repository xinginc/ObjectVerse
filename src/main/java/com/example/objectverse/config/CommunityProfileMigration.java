package com.example.objectverse.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class CommunityProfileMigration implements CommandLineRunner, WebMvcConfigurer {

    private final JdbcTemplate jdbcTemplate;
    private final Path uploadRoot = Paths.get("uploads").toAbsolutePath().normalize();

    public CommunityProfileMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws IOException {
        Files.createDirectories(uploadRoot.resolve("notes"));
        addColumnIgnoringExisting("ALTER TABLE user_account ADD COLUMN age INT AFTER status");
        addColumnIgnoringExisting("ALTER TABLE user_account ADD COLUMN occupation VARCHAR(128) AFTER age");
        addColumnIgnoringExisting("ALTER TABLE user_account ADD COLUMN bio TEXT AFTER occupation");
        addColumnIgnoringExisting("ALTER TABLE knowledge_note ADD COLUMN description TEXT AFTER category");
        addColumnIgnoringExisting("ALTER TABLE knowledge_note ADD COLUMN image_url VARCHAR(512) AFTER source_type");
        addColumnIgnoringExisting("ALTER TABLE knowledge_note ADD COLUMN tags VARCHAR(255) AFTER image_url");
        addColumnIgnoringExisting("ALTER TABLE knowledge_note ADD COLUMN visibility VARCHAR(32) NOT NULL DEFAULT 'PRIVATE' AFTER tags");
        addColumnIgnoringExisting("ALTER TABLE knowledge_note ADD COLUMN like_count INT NOT NULL DEFAULT 0 AFTER visibility");
        addColumnIgnoringExisting("ALTER TABLE knowledge_note ADD COLUMN favorite_count INT NOT NULL DEFAULT 0 AFTER like_count");
        addColumnIgnoringExisting("CREATE INDEX idx_knowledge_note_visibility ON knowledge_note (visibility)");
        executeIgnoringFailure("""
                CREATE TABLE IF NOT EXISTS knowledge_note_reaction (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    note_id BIGINT NOT NULL,
                    user_id BIGINT NOT NULL,
                    reaction_type VARCHAR(32) NOT NULL,
                    create_time DATETIME,
                    update_time DATETIME,
                    deleted TINYINT DEFAULT 0,
                    INDEX idx_note_reaction_note (note_id),
                    INDEX idx_note_reaction_user (user_id),
                    INDEX idx_note_reaction_active (note_id, user_id, reaction_type, deleted)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadRoot.toUri().toString());
    }

    private void addColumnIgnoringExisting(String sql) {
        executeIgnoringFailure(sql);
    }

    private void executeIgnoringFailure(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (DataAccessException ignored) {
            // Existing local databases may already contain these columns or indexes.
        }
    }
}
