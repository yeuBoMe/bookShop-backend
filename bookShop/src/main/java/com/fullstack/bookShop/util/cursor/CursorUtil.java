package com.fullstack.bookShop.util.cursor;

import com.fullstack.bookShop.util.constant.SymbolConstant;
import com.fullstack.bookShop.util.string.StringUtils;

/**
 * Ma hoa con tro phan trang: gia tri sap xep + id, ghep lai roi base64.
 *
 * <p>Phai co id di kem: hai ban ghi cung gia tri sap xep (cung gia, cung diem) ma chi
 * dua vao gia tri thi trang sau se lap lai hoac bo sot ban ghi.
 */
public final class CursorUtil {

    private static final String SEPARATOR = SymbolConstant.UNDERSCORE;

    private CursorUtil() {}

    public static String encode(Object sortValue, Long id) {
        return StringUtils.encodeBase64(sortValue + SEPARATOR + id);
    }

    /** Con tro hong (nguoi dung tu sua tren URL) coi nhu khong co con tro, tra ve null. */
    public static Cursor decode(String cursor) {
        if (StringUtils.isNullOrBlank(cursor)) {
            return null;
        }

        try {
            String decoded = StringUtils.decodeBase64(cursor);
            int separatorIndex = decoded.lastIndexOf(SEPARATOR);
            if (separatorIndex <= 0) {
                return null;
            }

            String sortValue = decoded.substring(0, separatorIndex);
            Long id = Long.parseLong(decoded.substring(separatorIndex + 1));
            return new Cursor(sortValue, id);

        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public record Cursor(String sortValue, Long id) {

        public Double asDouble() {
            return Double.parseDouble(sortValue);
        }

        public java.time.Instant asInstant() {
            return java.time.Instant.parse(sortValue);
        }
    }
}
