package com.fullstack.bookShop.dto.response;

/** Ket qua gom nhom danh gia theo sach: dung de ghi lai average_stars / rating_count tren books. */
public interface BookRatingSummaryResponse {

    Long getBookId();

    Double getAverageStars();

    Long getRatingCount();
}
