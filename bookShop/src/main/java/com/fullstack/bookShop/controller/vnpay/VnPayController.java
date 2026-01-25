package com.fullstack.bookShop.controller.vnpay;

import com.fullstack.bookShop.dto.response.payment.PaymentResponse;
import com.fullstack.bookShop.service.interfaces.VnPayService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.*;

@RestController
@RequestMapping("/api/v1/vnpay")
@RequiredArgsConstructor
public class VnPayController {

    private final VnPayService vnPayService;

    @PostMapping("/create-url")
    @ApiMessage(ActionConstant.VN_PAY)
    public ResponseEntity<PaymentResponse> createVnPayUrl(@RequestBody Map<String, Object> body)
            throws UnsupportedEncodingException {
        String paymentUrl = vnPayService.createPaymentUrl(body);
        return ResponseEntity.ok(new PaymentResponse(paymentUrl));
    }

    // SỬA: Endpoint này nhận callback từ VNPay và redirect về frontend
    @GetMapping("/vnpay-return")
    public void returnFromVNPay(
            @RequestParam Map<String, String> params,
            HttpServletResponse response
    ) throws Exception {
        vnPayService.handleVnPayReturn(params, response);
    }
}