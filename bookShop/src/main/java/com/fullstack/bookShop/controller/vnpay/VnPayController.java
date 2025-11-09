package com.fullstack.bookShop.controller.vnpay;

import com.fullstack.bookShop.config.VnPayConfig;
import com.fullstack.bookShop.domain.entity.Order;
import com.fullstack.bookShop.service.OrderService;
import com.fullstack.bookShop.util.constant.StatusEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/v1/vnpay")
@RequiredArgsConstructor
public class VnPayController {

    private final OrderService orderService;

    private final HttpServletRequest httpServletRequest;

    @GetMapping("/create/{orderId}")
    public String createVnPayUrl(@PathVariable Long orderId) throws UnsupportedEncodingException {
        Order orderGetById = orderService.getOrderById(orderId);
        Double amount = orderGetById.getTotalPrice();
        String vnp_TxnRef = orderId.toString();

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", VnPayConfig.vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf((long) (amount * 100)));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_BankCode", "NCB");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", VnPayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));

        cld.add(Calendar.MINUTE, 15);
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
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                //Build query
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

//    // -----------------------------
//    // 2️⃣ VNPAY GỌI VỀ - XÁC NHẬN GIAO DỊCH (IPN)
//    // -----------------------------
//    @PostMapping("/vnpay-ipn")
//    public ResponseEntity<String> ipn(@RequestParam Map<String, String> params) {
//        try {
//            // ✅ Xác thực chữ ký
//            String vnp_SecureHash = params.remove("vnp_SecureHash");
//            String vnp_SecureHashType = params.remove("vnp_SecureHashType");
//
//            String signValue = VnPayConfig.hashAllFields(params);
//            if (!signValue.equals(vnp_SecureHash)) {
//                return ResponseEntity.badRequest().body("Invalid signature");
//            }
//
//            // ✅ Kiểm tra mã phản hồi
//            if ("00".equals(params.get("vnp_ResponseCode"))) {
//                Long orderId = Long.parseLong(params.get("vnp_TxnRef"));
//                Order order = new Order();
//                order.setStatus(StatusEnum.valueOf("PAID"));
//                orderService.updateOrderPartialById(orderId, order);
//                return ResponseEntity.ok("Payment confirmed");
//            } else {
//                return ResponseEntity.ok("Payment failed");
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.internalServerError().body("Error processing IPN");
//        }
//    }

    // -----------------------------
    // 3️⃣ NGƯỜI DÙNG TRẢ VỀ SAU KHI THANH TOÁN (RETURN)
    // -----------------------------
    @GetMapping("/vnpay-return")
    public void returnFromVNPay(@RequestParam Map<String, String> params, HttpServletResponse response)
            throws IOException {
        if ("00".equals(params.get("vnp_ResponseCode"))) {
            response.sendRedirect("http://localhost:5173/checkout");
        } else {
            response.sendRedirect("http://localhost:5173/checkout/fail");
        }
    }
}
