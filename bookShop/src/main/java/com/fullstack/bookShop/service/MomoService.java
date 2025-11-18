package com.fullstack.bookShop.service;

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
}
