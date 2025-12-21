package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.config.VnPayConfig;
import com.fullstack.bookShop.service.VnPayService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VnPayServiceImpl implements VnPayService {

    @Value("${bookshop.frontend.url}")
    private String frontendUrl;

    @Value("${bookshop.backend.url}")
    private String backendUrl;

    @Override
    public String createPaymentUrl(Map<String, Object> body) throws UnsupportedEncodingException {
        Double amount = ((Number) body.get("amount")).doubleValue();
        String orderInfo = (String) body.get("orderInfo");
        String vnp_TxnRef = VnPayConfig.getRandomNumber(8);
        String vnp_ReturnUrl = backendUrl + "/api/v1/vnpay/vnpay-return";

        // FIX: Sử dụng múi giờ Việt Nam
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
        Calendar calendar = Calendar.getInstance(timeZone);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(timeZone);

        // Thời gian tạo giao dịch (hiện tại)
        String vnp_CreateDate = formatter.format(calendar.getTime());

        // Thời gian hết hạn (thêm 15 phút)
        calendar.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(calendar.getTime());

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", VnPayConfig.vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf((long) (amount * 100)));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_BankCode", "NCB");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfo != null ? orderInfo : "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Sort parameters
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                // Build query string
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = VnPayConfig.hmacSHA512(VnPayConfig.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = VnPayConfig.vnp_PayUrl + "?" + queryUrl;

        // Debug log (có thể bỏ sau khi test xong)
        System.out.println("=== VNPay Payment Debug ===");
        System.out.println("Create Date: " + vnp_CreateDate);
        System.out.println("Expire Date: " + vnp_ExpireDate);
        System.out.println("TxnRef: " + vnp_TxnRef);
        System.out.println("Amount: " + vnp_Params.get("vnp_Amount"));
        System.out.println("==========================");

        return paymentUrl;
    }

    @Override
    public void handleVnPayReturn(Map<String, String> params, HttpServletResponse response) throws Exception {
        String vnpResponseCode = params.get("vnp_ResponseCode");
        String vnpTxnRef = params.get("vnp_TxnRef");

        // Log return parameters
        System.out.println("=== VNPay Return Debug ===");
        System.out.println("Response Code: " + vnpResponseCode);
        System.out.println("TxnRef: " + vnpTxnRef);
        System.out.println("=========================");

        // Verify signature
        String vnp_SecureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        String signValue = VnPayConfig.hashAllFields(params);

        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(vnpResponseCode)) {
                // Payment success
                response.sendRedirect(frontendUrl + "/checkout?payment=vnpay&status=success&txnRef=" + vnpTxnRef);
            } else {
                // Payment failed
                response.sendRedirect(frontendUrl + "/checkout?payment=vnpay&status=fail&code=" + vnpResponseCode);
            }
        } else {
            // Invalid signature
            System.err.println("Invalid VNPay signature!");
            response.sendRedirect(frontendUrl + "/checkout?payment=vnpay&status=fail&code=97");
        }
    }
}
