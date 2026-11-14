package com.fullstack.bookShop.service.interfaces;

import com.fullstack.bookShop.dto.request.BookSearchRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Xuat du lieu ra CSV, ghi thang xuong luong dau ra cua response.
 *
 * <p>Vi sao khong tra ve mot chuoi hay mot List roi de Spring serialize: lam vay la
 * gom TOAN BO du lieu vao bo nho truoc khi gui di - 100.000 dong la het RAM.
 * O day doc theo lo va ghi ngay tung lo ra ngoai, bo nho dung khong doi theo so dong.
 *
 * <p>Ham nhan thang HttpServletResponse va tra ve void: controller tra void kem
 * HttpServletResponse thi Spring coi request da xu ly xong, khong chay ResponseBodyAdvice -
 * nho vay FormatGlobalResponse khong boc luong byte vao GlobalResponse.
 */
public interface ExportService {

    void exportBooksToCsv(HttpServletResponse response, BookSearchRequest filter) throws IOException;

    void exportOrdersToCsv(HttpServletResponse response, String status) throws IOException;
}
