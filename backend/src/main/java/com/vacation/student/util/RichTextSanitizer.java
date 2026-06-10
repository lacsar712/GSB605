package com.vacation.student.util;

public final class RichTextSanitizer {

    private RichTextSanitizer() {
    }

    public static String sanitize(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }

        String sanitized = html;
        sanitized = sanitized.replaceAll("(?is)<script[^>]*>.*?</script>", "");
        sanitized = sanitized.replaceAll("(?is)<style[^>]*>.*?</style>", "");
        sanitized = sanitized.replaceAll("(?is)<iframe[^>]*>.*?</iframe>", "");
        sanitized = sanitized.replaceAll("(?is)<object[^>]*>.*?</object>", "");
        sanitized = sanitized.replaceAll("(?is)<embed[^>]*>.*?</embed>", "");
        sanitized = sanitized.replaceAll("(?i)\\son[a-z]+\\s*=\\s*(['\"]).*?\\1", "");
        sanitized = sanitized.replaceAll("(?i)\\son[a-z]+\\s*=\\s*[^\\s>]+", "");
        sanitized = sanitized.replaceAll("(?i)(href|src)\\s*=\\s*(['\"])javascript:.*?\\2", "$1=\"#\"");
        return sanitized.trim();
    }
}
