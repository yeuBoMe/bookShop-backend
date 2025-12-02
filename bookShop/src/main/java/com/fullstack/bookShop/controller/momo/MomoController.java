package com.fullstack.bookShop.controller.momo;

import com.fullstack.bookShop.dto.payment.RestPaymentDTO;
import com.fullstack.bookShop.service.MomoService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/momo")
@RequiredArgsConstructor
public class MomoController {

    private final MomoService momoService;

    @PostMapping("/create-url")
    public ResponseEntity<RestPaymentDTO> testPayment(@RequestBody Map<String, Object> body) throws Exception {
        String response = momoService.createPaymentRequest(body);
        return ResponseEntity.ok(new RestPaymentDTO(response));
    }

    @PostMapping("/ipn")
    public ResponseEntity<String> ipn(@RequestBody Map<String, String> params) {
        momoService.handleIpn(params);
        return ResponseEntity.ok("OK");
    }

    // Quan trọng: Xử lý return giống VNPay
    @GetMapping("/momo-return")
    public void momoReturn(@RequestParam Map<String, String> params, HttpServletResponse response) throws IOException {
        momoService.handleMomoReturn(params, response);
    }
}
