package com.samuelmaia1_github.yourauth.domain.shared;

public final class SafeLog {
    private static final String ABSENT = "ausente";

    private SafeLog() {
    }

    public static boolean present(String value) {
        return value != null && !value.isBlank();
    }

    public static String maskEmail(String email) {
        if (!present(email)) {
            return ABSENT;
        }

        String sanitized = singleLine(email);
        int at = sanitized.indexOf('@');

        if (at <= 0 || at == sanitized.length() - 1) {
            return "***";
        }

        return sanitized.charAt(0) + "***@" + sanitized.substring(at + 1);
    }

    public static String maskCpf(String cpf) {
        if (!present(cpf)) {
            return ABSENT;
        }

        String digits = cpf.replaceAll("\\D", "");

        if (digits.length() < 4) {
            return "***";
        }

        return "***" + digits.substring(digits.length() - 4);
    }

    public static String fingerprint(String value) {
        if (!present(value)) {
            return ABSENT;
        }

        String sanitized = singleLine(value);
        int length = sanitized.length();

        if (length <= 8) {
            return "len=" + length;
        }

        return sanitized.substring(0, 4)
                + "..."
                + sanitized.substring(length - 4)
                + "(len="
                + length
                + ")";
    }

    public static String compact(String value, int maxLength) {
        if (!present(value)) {
            return ABSENT;
        }

        String sanitized = singleLine(value);

        if (sanitized.length() <= maxLength) {
            return sanitized;
        }

        return sanitized.substring(0, Math.max(0, maxLength))
                + "...(len="
                + sanitized.length()
                + ")";
    }

    private static String singleLine(String value) {
        return value.replaceAll("[\\r\\n\\t]+", " ").trim();
    }
}
