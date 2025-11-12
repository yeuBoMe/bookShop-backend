package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.config.VnPayConfig;
import com.fullstack.bookShop.service.VnPayService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VnPayServiceImpl implements VnPayService {

    @Override
    public String createPaymentUrl(Map<String, Object> body) throws UnsupportedEncodingException {
        Double amount = ((Number) body.get("amount")).doubleValue();
        String orderInfo = (String) body.get("orderInfo");
        String vnp_TxnRef = VnPayConfig.getRandomNumber(8);

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
        vnp_Params.put("vnp_ReturnUrl", VnPayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        cld.add(Calendar.MINUTE, 15);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));
        vnp_Params.put("vnp_ExpireDate", formatter.format(cld.getTime()));

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

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

        return paymentUrl;
    }

    @Override
    public void handleVnPayReturn(Map<String, String> params, HttpServletResponse response) throws Exception {
        String vnpResponseCode = params.get("vnp_ResponseCode");

        // Verify signature
        String vnp_SecureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        String signValue = VnPayConfig.hashAllFields(params);

        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(vnpResponseCode)) {
                // Case success
                response.sendRedirect("http://localhost:5173/checkout#vnpay-success");
            } else {
                // Case fail
                response.sendRedirect("http://localhost:5173/checkout/fail?code=" + vnpResponseCode);
            }
        } else {
            // Invalid signature
            response.sendRedirect("http://localhost:5173/checkout/fail?code=97");
        }
    }
}
