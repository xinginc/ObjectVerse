package com.example.objectverse.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Component
public class GeneratedCodeHeaderCleanup implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public GeneratedCodeHeaderCleanup(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT id, code_content
                FROM code_file
                WHERE deleted = 0
                  AND code_content LIKE '%ObjectVerse%自动生成%'
                """);
        for (Map<String, Object> row : rows) {
            Long id = ((Number) row.get("id")).longValue();
            String original = String.valueOf(row.get("code_content"));
            String cleaned = stripLegacyFileHeader(original);
            if (!cleaned.equals(original)) {
                jdbcTemplate.update(
                        "UPDATE code_file SET code_content = ?, update_time = NOW() WHERE id = ?",
                        cleaned,
                        id
                );
            }
        }
    }

    private String stripLegacyFileHeader(String codeContent) {
        if (!StringUtils.hasText(codeContent)) {
            return codeContent;
        }
        return codeContent.replaceFirst(
                "(?s)^\\s*/\\*\\*.*?ObjectVerse\\s*自动生成.*?\\*/\\s*",
                ""
        );
    }
}
