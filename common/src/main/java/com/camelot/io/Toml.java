package com.camelot.io;

import com.camelot.config.ConfigOption;
import com.camelot.config.ConfigSchema;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Toml {

    private static final char QUOTE = 34;
    private static final char APOSTROPHE = 39;
    private static final char BACKSLASH = 92;

    private Toml() {
    }

    public static Map<String, Object> parse(String text) {
        Map<String, Object> out = new LinkedHashMap<>();
        String[] lines = text.split("\\R", -1);
        String section = null;
        for (int i = 0; i < lines.length; i++) {
            String line = stripComment(lines[i]).trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("[") && line.endsWith("]")) {
                section = unquote(line.substring(1, line.length() - 1));
                continue;
            }
            int eq = line.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String key = unquote(line.substring(0, eq).trim());
            String value = line.substring(eq + 1).trim();
            if (value.startsWith("[") && !value.endsWith("]")) {
                StringBuilder joined = new StringBuilder(value);
                while (++i < lines.length) {
                    joined.append(' ').append(stripComment(lines[i]).trim());
                    if (joined.toString().trim().endsWith("]")) {
                        break;
                    }
                }
                value = joined.toString().trim();
            }

            out.put(section != null && "blacklist".equals(key) ? section : key, parseValue(value));
        }
        return out;
    }

    public static List<String> unnest(String text) {
        int open = text.indexOf('[');
        int close = text.lastIndexOf(']');
        if (open < 0 || close <= open) {
            return List.of();
        }
        Object parsed = parseValue(text.substring(open, close + 1));
        return parsed instanceof List<?> list ? list.stream().map(String::valueOf).toList() : List.of();
    }

    public static String write(ConfigSchema schema, Map<String, Object> values) {
        StringBuilder out = new StringBuilder();
        for (ConfigOption option : schema.options()) {
            if (option.comment() != null) {
                out.append("# ").append(option.comment()).append('\n');
            }
            out.append(option.key()).append(" = ")
                    .append(format(values.get(option.key()))).append('\n');
        }
        return out.toString();
    }

    private static String format(Object value) {
        if (value instanceof Boolean flag) {
            return flag ? "true" : "false";
        }
        if (value instanceof Number number) {
            return String.valueOf(number.intValue());
        }
        if (value instanceof List<?> list) {
            StringBuilder out = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) {
                    out.append(", ");
                }
                out.append(QUOTE).append(escape(String.valueOf(list.get(i)))).append(QUOTE);
            }
            return out.append(']').toString();
        }
        return QUOTE + escape(String.valueOf(value)) + QUOTE;
    }

    private static String escape(String text) {
        StringBuilder out = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == BACKSLASH || c == QUOTE) {
                out.append(BACKSLASH);
            }
            out.append(c);
        }
        return out.toString();
    }

    private static String unescape(String text) {
        StringBuilder out = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == BACKSLASH && i + 1 < text.length()) {
                c = text.charAt(++i);
            }
            out.append(c);
        }
        return out.toString();
    }

    private static Object parseValue(String raw) {
        String value = raw.trim();
        if (value.startsWith("[")) {
            List<String> items = new ArrayList<>();
            String inner = value.substring(1, value.endsWith("]") ? value.length() - 1 : value.length());
            for (String part : splitList(inner)) {
                String item = part.trim();
                if (!item.isEmpty()) {
                    items.add(unescape(unquote(item)));
                }
            }
            return items;
        }
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }
        if (value.isEmpty()) {
            return "";
        }
        char first = value.charAt(0);
        if (first == QUOTE || first == APOSTROPHE) {
            return unescape(unquote(value));
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return unescape(value);
        }
    }

    private static List<String> splitList(String inner) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == QUOTE) {
                quoted = !quoted;
            }
            if (c == ',' && !quoted) {
                parts.add(current.toString());
                current.setLength(0);
                continue;
            }
            current.append(c);
        }
        if (!current.isEmpty()) {
            parts.add(current.toString());
        }
        return parts;
    }

    private static String stripComment(String line) {
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == QUOTE) {
                quoted = !quoted;
            }
            if (c == '#' && !quoted) {
                return line.substring(0, i);
            }
        }
        return line;
    }

    private static String unquote(String text) {
        String value = text.trim();
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == QUOTE || first == APOSTROPHE) && first == last) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
