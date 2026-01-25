package com.fullstack.bookShop.service.interfaces;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Map;

public interface MomoService {

    /**
     * Tạo link thanh toán MoMo (QR + Deep link app)
     */
    String createPaymentRequest(Map<String, Object> body) throws Exception;

    /**
     * Xử lý callback IPN từ MoMo (gọi khi thanh toán xong)
     */
    boolean handleIpn(Map<String, String> params);

    void handleMomoReturn(
            @RequestParam Map<String, String> params,
            HttpServletResponse response
    ) throws IOException;
}
