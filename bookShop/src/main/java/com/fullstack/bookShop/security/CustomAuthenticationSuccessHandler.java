package com.fullstack.bookShop.security;

import com.fullstack.bookShop.dto.response.auth.LoginResponse;
import com.fullstack.bookShop.model.User;
import com.fullstack.bookShop.service.interfaces.UserService;
import com.fullstack.bookShop.util.constant.auth.TokenConstant;
import com.fullstack.bookShop.util.login.UserLoginResponseUtil;
import com.fullstack.bookShop.util.security.SecurityUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${bookshop.frontend.url}")
    private String frontendUrl;

    @Value("${bookshop.jwt.refresh-token-validity-in-seconds}")
    private Long refreshTokenExpiration;

    private final SecurityUtil securityUtil;

    private final UserService userService;

    // hàm Spring gọi khi Google login thành công.
    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        // Lấy user vừa authenticate, set trong CustomDefaultOAuth2UserService.
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        User userCreatedFromGoogle = userService.createGoogleUser(email, name, picture);
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setUser(UserLoginResponseUtil.buildUserLogin(userCreatedFromGoogle));

        String accessToken = securityUtil.createAccessToken(email, loginResponse);
        String refreshToken = securityUtil.createRefreshToken(email, loginResponse);
        userService.saveUserRefreshToken(email, refreshToken);

        // Lưu refresh_token vào cookie
        Cookie cookie = new Cookie(TokenConstant.REFRESH_TOKEN, refreshToken);
        cookie.setPath("/");
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(refreshTokenExpiration.intValue());
        response.addCookie(cookie);

        String redirectUrl = String.format(
                frontendUrl + "?access_token=%s&google_login=success",
                accessToken
        );

        response.sendRedirect(redirectUrl);
    }
}




