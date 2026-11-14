package com.fullstack.bookShop.controller.export;

import com.fullstack.bookShop.dto.request.BookSearchRequest;
import com.fullstack.bookShop.service.interfaces.ExportService;
import com.fullstack.bookShop.util.annotation.IsAdmin;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Xuat du lieu ra CSV theo luong.
 *
 * <p>Cac ham CO Y tra ve void va nhan HttpServletResponse: Spring coi request da duoc
 * xu ly xong nen KHONG chay ResponseBodyAdvice, nho vay FormatGlobalResponse khong boc
 * luong byte vao GlobalResponse. Cung vi the o day khong dat @ApiMessage.
 *
 * <p>Luu y khi doc log: loi xay ra GIUA chung luong thi khong doi duoc HTTP status nua
 * (header da gui di roi), file tai ve se bi cut. Do la danh doi cua viec stream.
 */
@RestController
@RequestMapping("/api/v1/exports")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @IsAdmin
    @GetMapping("/books.csv")
    public void exportBooksRequest(
            BookSearchRequest bookSearchRequest,
            HttpServletResponse response
    ) throws IOException {
        exportService.exportBooksToCsv(response, bookSearchRequest);
    }

    @IsAdmin
    @GetMapping("/orders.csv")
    public void exportOrdersRequest(
            @RequestParam(required = false) String status,
            HttpServletResponse response
    ) throws IOException {
        exportService.exportOrdersToCsv(response, status);
    }
}
