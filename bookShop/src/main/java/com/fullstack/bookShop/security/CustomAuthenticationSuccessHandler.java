package com.fullstack.bookShop.security;

import com.fullstack.bookShop.dto.auth.RestLoginDTO;
import com.fullstack.bookShop.model.Permission;
import com.fullstack.bookShop.model.User;
import com.fullstack.bookShop.service.UserService;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${bookshop.jwt.refresh-token-validity-in-seconds}")
    private Long refreshTokenExpiration;

    private final SecurityUtil securityUtil;

    private final UserService userService;

    // hàm Spring gọi khi Google login thành công.
    @Override
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
        RestLoginDTO restLoginDTO = new RestLoginDTO();
        restLoginDTO.setUser(buildUserLogin(userCreatedFromGoogle));

        String accessToken = securityUtil.createAccessToken(email, restLoginDTO);
        String refreshToken = securityUtil.createRefreshToken(email, restLoginDTO);
        userService.handleSaveUserRefreshToken(email, refreshToken);

        // Lưu refresh_token vào cookie
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(refreshTokenExpiration.intValue());
        cookie.setSecure(true);
        response.addCookie(cookie);

        String redirectUrl = String.format(
                "http://localhost:5173?access_token=%s&google_login=success",
                accessToken
        );

        response.sendRedirect(redirectUrl);
    }

    private RestLoginDTO.UserLogin buildUserLogin(User user) {
        if (user == null) {
            return null;
        }

        if (user.getRole() != null) {
            List<String> permissionList = Optional.ofNullable(user.getRole().getPermissions())
                    .map(permissions -> permissions.stream()
                            .map(Permission::getName)
                            .toList()
                    )
                    .orElse(new ArrayList<>());

            RestLoginDTO.UserLogin.RoleOfUserLogin roleOfUserLogin
                    = new RestLoginDTO.UserLogin.RoleOfUserLogin(user.getRole().getName(), permissionList);

            return new RestLoginDTO.UserLogin(
                    user.getId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getAvatar(),
                    user.getPhone(),
                    roleOfUserLogin
            );
        }

        return new RestLoginDTO.UserLogin(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getAvatar(),
                user.getPhone(),
                null
        );
    }
}




