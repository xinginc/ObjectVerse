package com.example.objectverse.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProjectVisibilityMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public ProjectVisibilityMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        executeIgnoringExistingColumn("""
                ALTER TABLE project
                ADD COLUMN visibility VARCHAR(32) NOT NULL DEFAULT 'PRIVATE' AFTER status
                """);
        executeIgnoringExistingColumn("""
                CREATE INDEX idx_project_visibility ON project (visibility)
                """);
    }

    private void executeIgnoringExistingColumn(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (DataAccessException ignored) {
            // The column or index already exists in an initialized local database.
        }
    }
}
