package com.fullstack.bookShop.util.string;

import java.text.Normalizer;
import java.util.Base64;
import java.util.List;

/**
 * Gom mọi thao tác trim / blank / normalize chuỗi về một chỗ.
 * Quy ước chốt: chuỗi chỉ-toàn-khoảng-trắng quy về {@code null} — một tín hiệu "không có giá trị" duy nhất.
 */
public final class StringUtils {

    private StringUtils() {}

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }

    /** Khác {@link #isNullOrEmpty}: {@code "   "} cũng coi là blank. */
    public static boolean isNullOrBlank(String value) {
        return value == null || value.isBlank();
    }

    public static boolean hasValue(String value) {
        return !isNullOrBlank(value);
    }

    /** Trim giữ nguyên chuỗi rỗng — dùng khi caller đã tự kiểm tra blank. */
    public static String convertValueTrim(String value) {
        return !isNullOrEmpty(value) ? value.trim() : value;
    }

    public static String convertValueTrimUpper(String value) {
        return !isNullOrEmpty(value) ? value.trim().toUpperCase() : value;
    }

    public static String convertValueTrimLower(String value) {
        return !isNullOrEmpty(value) ? value.trim().toLowerCase() : value;
    }

    /** {@code " ABC " → "ABC"} · {@code "   " → null} · {@code "" → null} · {@code null → null}. */
    public static String trimBlank(String value) {
        return !isNullOrBlank(value) ? value.trim() : null;
    }

    public static String trimUpperBlank(String value) {
        return !isNullOrBlank(value) ? value.trim().toUpperCase() : null;
    }

    public static String trimLowerBlank(String value) {
        return !isNullOrBlank(value) ? value.trim().toLowerCase() : null;
    }

    /** Trim hai đầu + gộp mọi khoảng trắng giữa các từ về một dấu cách. */
    public static String normalizeSpaces(String value) {
        String trimmed = trimBlank(value);
        if (trimmed == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(trimmed.length());
        boolean previousIsSpace = false;
        for (int i = 0; i < trimmed.length(); i++) {
            char character = trimmed.charAt(i);
            if (Character.isWhitespace(character)) {
                if (!previousIsSpace) {
                    builder.append(' ');
                    previousIsSpace = true;
                }
            } else {
                builder.append(character);
                previousIsSpace = false;
            }
        }
        return builder.toString();
    }

    /** Bỏ dấu tiếng Việt: {@code "Tiếng Việt" → "Tieng Viet"} — dùng cho so khớp tìm kiếm không dấu. */
    public static String removeDiacriticalMarks(String value) {
        if (value == null) {
            return null;
        }

        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD);
        StringBuilder builder = new StringBuilder(decomposed.length());
        decomposed.codePoints()
                .filter(codePoint -> Character.getType(codePoint) != Character.NON_SPACING_MARK)
                .forEach(builder::appendCodePoint);

        return builder.toString()
                .replace('Đ', 'D')
                .replace('đ', 'd');
    }

    /** Khoá so sánh chuẩn hoá: trim + gộp space + bỏ dấu + lowercase. */
    public static String toSearchKey(String value) {
        String normalized = normalizeSpaces(value);
        return normalized != null ? removeDiacriticalMarks(normalized).toLowerCase() : null;
    }

    public static String capitalizeFirstLetter(String value) {
        String trimmed = trimBlank(value);
        return trimmed != null ? trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1) : null;
    }

    public static String join(List<?> list, String separator) {
        return org.apache.commons.lang3.StringUtils.join(list, separator);
    }

    public static String encodeBase64(String value) {
        return value != null
                ? Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes())
                : null;
    }

    public static String decodeBase64(String value) {
        return value != null ? new String(Base64.getUrlDecoder().decode(value)) : null;
    }
}
