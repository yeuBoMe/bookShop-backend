package com.fullstack.bookShop.util.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** So khop chu ky cua cong thanh toan. */
public final class SignatureUtil {

    private SignatureUtil() {}

    /**
     * So sanh chu ky theo kieu hang thoi gian.
     *
     * <p>{@code String.equals} dung sau ky tu dau tien khac nhau, nen thoi gian tra loi
     * he lo dan tung ky tu cua chu ky dung. Voi luong thanh toan thi do la duong de
     * do dan chu ky hop le.
     */
    public static boolean matches(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }

        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8)
        );
    }
}
