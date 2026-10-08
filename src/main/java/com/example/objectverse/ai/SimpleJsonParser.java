package com.example.objectverse.ai;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SimpleJsonParser {

    private final String text;
    private int index;

    private SimpleJsonParser(String text) {
        this.text = text == null ? "" : text;
    }

    public static Map<String, Object> parseObject(String json) {
        Object value = new SimpleJsonParser(json).parseValue();
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                result.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            return result;
        }
        return new LinkedHashMap<>();
    }

    private Object parseValue() {
        skipWhitespace();
        if (index >= text.length()) {
            return null;
        }
        char current = text.charAt(index);
        if (current == '{') {
            return parseMap();
        }
        if (current == '[') {
            return parseList();
        }
        if (current == '"') {
            return parseString();
        }
        if (text.startsWith("true", index)) {
            index += 4;
            return Boolean.TRUE;
        }
        if (text.startsWith("false", index)) {
            index += 5;
            return Boolean.FALSE;
        }
        if (text.startsWith("null", index)) {
            index += 4;
            return null;
        }
        return parseLiteral();
    }

    private Map<String, Object> parseMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        index++;
        skipWhitespace();
        while (index < text.length() && text.charAt(index) != '}') {
            String key = parseString();
            skipWhitespace();
            if (index < text.length() && text.charAt(index) == ':') {
                index++;
            }
            Object value = parseValue();
            map.put(key, value);
            skipWhitespace();
            if (index < text.length() && text.charAt(index) == ',') {
                index++;
                skipWhitespace();
            }
        }
        if (index < text.length() && text.charAt(index) == '}') {
            index++;
        }
        return map;
    }

    private List<Object> parseList() {
        List<Object> list = new ArrayList<>();
        index++;
        skipWhitespace();
        while (index < text.length() && text.charAt(index) != ']') {
            list.add(parseValue());
            skipWhitespace();
            if (index < text.length() && text.charAt(index) == ',') {
                index++;
                skipWhitespace();
            }
        }
        if (index < text.length() && text.charAt(index) == ']') {
            index++;
        }
        return list;
    }

    private String parseString() {
        StringBuilder builder = new StringBuilder();
        if (index < text.length() && text.charAt(index) == '"') {
            index++;
        }
        while (index < text.length()) {
            char current = text.charAt(index++);
            if (current == '"') {
                break;
            }
            if (current == '\\' && index < text.length()) {
                char escaped = text.charAt(index++);
                if (escaped == 'n') {
                    builder.append('\n');
                } else if (escaped == 't') {
                    builder.append('\t');
                } else if (escaped == 'r') {
                    builder.append('\r');
                } else if (escaped == 'u' && index + 4 <= text.length()) {
                    builder.append((char) Integer.parseInt(text.substring(index, index + 4), 16));
                    index += 4;
                } else {
                    builder.append(escaped);
                }
            } else {
                builder.append(current);
            }
        }
        return builder.toString();
    }

    private String parseLiteral() {
        int start = index;
        while (index < text.length()) {
            char current = text.charAt(index);
            if (current == ',' || current == '}' || current == ']' || Character.isWhitespace(current)) {
                break;
            }
            index++;
        }
        return text.substring(start, index);
    }

    private void skipWhitespace() {
        while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
            index++;
        }
    }
}
