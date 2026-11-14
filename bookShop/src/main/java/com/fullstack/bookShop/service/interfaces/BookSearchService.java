package com.fullstack.bookShop.service.interfaces;

import com.fullstack.bookShop.dto.request.BookSearchRequest;
import com.fullstack.bookShop.dto.response.BookResponse;
import com.fullstack.bookShop.dto.response.cursor.CursorPageResponse;

/**
 * Tim kiem sach phan trang theo con tro.
 *
 * <p>Khac /api/v1/books o cho khong dem tong va khong dung OFFSET, nen trang thu 250
 * cung nhanh nhu trang dau. Doi lai khong biet tong so trang - phu hop cuon vo han
 * hoac nut "xem them", khong phu hop bang co so trang.
 */
public interface BookSearchService {

    CursorPageResponse<BookResponse> search(BookSearchRequest request);
}
