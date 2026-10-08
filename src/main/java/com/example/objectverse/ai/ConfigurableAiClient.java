package com.example.objectverse.ai;

import com.example.objectverse.config.AiProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Primary
@Component
public class ConfigurableAiClient implements AiClient {

    private static final String SYSTEM_PROMPT = "你是 ObjectVerse 的 AI 面向对象建模助手。你必须严格返回 JSON，不要输出 Markdown，不要输出解释性废话。";

    private final AiProperties aiProperties;
    private final MockAiClient mockAiClient;
    private final HttpClient httpClient;

    public ConfigurableAiClient(
            AiProperties aiProperties,
            MockAiClient mockAiClient
    ) {
        this.aiProperties = aiProperties;
        this.mockAiClient = mockAiClient;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(12))
                .build();
    }

    @Override
    public String chat(String prompt) {
        if (shouldUseMock()) {
            return mockAiClient.chat(prompt);
        }
        try {
            String body = buildRequestBody(prompt);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(resolveChatCompletionsUrl()))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + aiProperties.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                System.out.println("[ObjectVerse AI] HTTP status " + response.statusCode()
                        + ", fallback to MockAiClient. Body: " + response.body());
                return mockAiClient.chat(prompt);
            }
            String content = extractContent(response.body());
            if (!StringUtils.hasText(content)) {
                System.out.println("[ObjectVerse AI] Empty choices[0].message.content, fallback to MockAiClient.");
                return mockAiClient.chat(prompt);
            }
            return content;
        } catch (Exception ex) {
            System.out.println("[ObjectVerse AI] API call failed, fallback to MockAiClient. Error: " + ex.getMessage());
            return mockAiClient.chat(prompt);
        }
    }

    private boolean shouldUseMock() {
        String provider = aiProperties.getProvider();
        if (!StringUtils.hasText(provider) || "mock".equalsIgnoreCase(provider)) {
            return true;
        }
        return !StringUtils.hasText(aiProperties.getApiKey())
                || !StringUtils.hasText(aiProperties.getBaseUrl())
                || !StringUtils.hasText(aiProperties.getModel());
    }

    private String resolveChatCompletionsUrl() {
        String baseUrl = aiProperties.getBaseUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + "/chat/completions";
    }

    private String buildRequestBody(String prompt) {
        return "{"
                + "\"model\":\"" + escape(aiProperties.getModel()) + "\","
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"" + escape(SYSTEM_PROMPT) + "\"},"
                + "{\"role\":\"user\",\"content\":\"" + escape(prompt) + "\"}"
                + "],"
                + "\"temperature\":0.2,"
                + "\"stream\":false"
                + "}";
    }

    private String extractContent(String responseBody) {
        String marker = "\"content\"";
        int markerIndex = responseBody.indexOf(marker);
        if (markerIndex < 0) {
            System.out.println("[ObjectVerse AI] choices[0].message.content not found in response.");
            return "";
        }
        int colonIndex = responseBody.indexOf(':', markerIndex + marker.length());
        int quoteIndex = responseBody.indexOf('"', colonIndex + 1);
        if (colonIndex < 0 || quoteIndex < 0) {
            System.out.println("[ObjectVerse AI] Unable to parse content field from response.");
            return "";
        }
        StringBuilder builder = new StringBuilder();
        boolean escaped = false;
        for (int i = quoteIndex + 1; i < responseBody.length(); i++) {
            char current = responseBody.charAt(i);
            if (current == '"' && !escaped) {
                break;
            }
            builder.append(current);
            if (escaped) {
                escaped = false;
            } else if (current == '\\') {
                escaped = true;
            }
        }
        return unescape(builder.toString());
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String unescape(String value) {
        return value.replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }
}
