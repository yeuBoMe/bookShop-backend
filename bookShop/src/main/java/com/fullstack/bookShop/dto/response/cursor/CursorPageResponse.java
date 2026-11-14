package com.fullstack.bookShop.dto.response.cursor;

import java.util.List;

/**
 * Trang du lieu theo con tro, KHONG co tong so ban ghi.
 *
 * <p>Do la chu y: dem tong bang count(*) la mot lan quet bang nua cho MOI request,
 * va OFFSET sau vai nghin dong van phai doc roi vut bo tung ay dong. Con tro thi
 * khong phu thuoc do sau cua trang.
 *
 * @param nextCursor null khi da het du lieu
 */
public record CursorPageResponse<T>(List<T> items, String nextCursor, boolean hasNext, int size) {

    public static <T> CursorPageResponse<T> of(List<T> items, String nextCursor) {
        return new CursorPageResponse<>(items, nextCursor, nextCursor != null, items.size());
    }
}
