package com.fullstack.bookShop.service;

import jakarta.servlet.http.HttpServletResponse;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface VnPayService {

    String createPaymentUrl(Map<String, Object> body) throws UnsupportedEncodingException;

    void handleVnPayReturn(Map<String, String> params, HttpServletResponse response) throws Exception;
}
