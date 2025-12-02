package com.fullstack.bookShop.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fullstack.bookShop.service.MomoService;
import jakarta.servlet.http.HttpServletResponse;
import net.minidev.json.JSONObject;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MomoServiceImpl implements MomoService {

    @Value("${bookshop.frontend.url}")
    private String frontendUrl;

    @Value("${bookshop.backend.url}")
    private String backendUrl;

    private static final String PARTNER_CODE = "MOMO";
    private static final String ACCESS_KEY = "F8BBA842ECF85";
    private static final String SECRET_KEY = "K951B6PE1waDMi640xX08PD3vg6EkVlz";
//    private static final String REDIRECT_URL = "http://localhost:8080/api/v1/momo/momo-return";
    private static final String IPN_URL = "https://callback.url/notify"; // thay bằng URL thật khi deploy

    @Override
    public String createPaymentRequest(Map<String, Object> body) throws Exception {
        Long amount = ((Number) body.get("amount")).longValue();
        String orderId = "MOMO" + System.currentTimeMillis();
        String requestId = orderId;
        String REDIRECT_URL = backendUrl + "/api/v1/momo/momo-return";

        String rawSignature = "accessKey=" + ACCESS_KEY +
                "&amount=" + amount +
                "&extraData=" +
                "&ipnUrl=" + IPN_URL +
                "&orderId=" + orderId +
                "&orderInfo=Thanh toan don hang " + orderId +
                "&partnerCode=" + PARTNER_CODE +
                "&redirectUrl=" + REDIRECT_URL +
                "&requestId=" + requestId +
                "&requestType=captureWallet";

        String signature = hmacSHA256(rawSignature, SECRET_KEY);

        JSONObject json = new JSONObject();
        json.put("partnerCode", PARTNER_CODE);
        json.put("accessKey", ACCESS_KEY);
        json.put("requestId", requestId);
        json.put("amount", amount);
        json.put("orderId", orderId);
        json.put("orderInfo", "Thanh toan don hang " + orderId);
        json.put("redirectUrl", REDIRECT_URL);
        json.put("ipnUrl", IPN_URL);
        json.put("extraData", "");
        json.put("requestType", "captureWallet");
        json.put("signature", signature);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost("https://test-payment.momo.vn/v2/gateway/api/create");
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(json.toJSONString(), StandardCharsets.UTF_8));

            String result = new BufferedReader(new InputStreamReader(
                    client.execute(post).getEntity().getContent(), StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining());

            JsonNode node = new ObjectMapper().readTree(result);
            if (node.has("payUrl")) {
                return node.get("payUrl").asText();
            }
            throw new RuntimeException("MoMo error: " + node.get("message").asText());
        }
    }

    private String hmacSHA256(String data, String key) throws Exception {
        Mac sha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256.init(secretKey);
        byte[] hash = sha256.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    @Override
    public boolean handleIpn(Map<String, String> params) {
        try {
            String receivedSig = params.get("signature");
            params.remove("signature");
            String raw = "accessKey=" + ACCESS_KEY +
                    "&orderId=" + params.get("orderId") +
                    "&partnerCode=" + PARTNER_CODE +
                    "&requestId=" + params.get("requestId");
            String mySig = hmacSHA256(raw, SECRET_KEY);

            if (mySig.equals(receivedSig) && "0".equals(params.get("resultCode"))) {
                // TODO: cập nhật đơn hàng thành PAID theo orderId
                System.out.println("MoMo IPN success: " + params.get("orderId"));
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void handleMomoReturn(Map<String, String> params, HttpServletResponse response) throws IOException {
        String resultCode = params.get("resultCode");

        if ("0".equals(resultCode)) {
            response.sendRedirect(frontendUrl + "/checkout#momo-success");
        } else {
            String code = params.get("resultCode") != null ? params.get("resultCode") : "99";
            response.sendRedirect(frontendUrl + "/checkout#momo-fail?code=" + code);
        }
    }
}
