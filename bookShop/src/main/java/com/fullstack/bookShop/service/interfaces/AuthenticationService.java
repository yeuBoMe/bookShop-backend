package com.fullstack.bookShop.service.interfaces;

import com.fullstack.bookShop.dto.request.auth.LoginRequest;
import com.fullstack.bookShop.dto.response.auth.LoginResponse;
import org.springframework.http.ResponseCookie;

public interface AuthenticationService {

    LoginResponse login(LoginRequest loginRequest);

    LoginResponse refresh(String refreshTokenOld);

    LoginResponse.UserGetAccountResponse account();

    ResponseCookie buildRefreshTokenCookie(String refreshToken);

    ResponseCookie clearRefreshTokenCookie();

    String generateAndSaveRefreshToken(String email, LoginResponse loginResponse);

    void logout();
}

