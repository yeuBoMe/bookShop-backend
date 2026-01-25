package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.dto.request.auth.LoginRequest;
import com.fullstack.bookShop.dto.response.auth.LoginResponse;
import com.fullstack.bookShop.model.User;
import com.fullstack.bookShop.service.interfaces.AuthenticationService;
import com.fullstack.bookShop.service.interfaces.UserService;
import com.fullstack.bookShop.util.constant.auth.TokenConstant;
import com.fullstack.bookShop.util.login.UserLoginResponseUtil;
import com.fullstack.bookShop.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    @Value("${bookshop.jwt.refresh-token-validity-in-seconds}")
    private Long refreshTokenExpiration;

    private final UserService userService;

    private final SecurityUtil securityUtil;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // class contains login info not validate
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
                = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());

        /* AuthenticationManagerBuilder calls AuthenticationManager to validate login info
         * then AuthenticationManager will use func loadUserByUsername in CustomUserDetailsService to
         * compare duplicated data before validate right/wrong login request
         */
        Authentication authentication = authenticationManagerBuilder.getObject()
                .authenticate(usernamePasswordAuthenticationToken);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User userGetByEmail = userService.getUserByEmail(loginRequest.getUsername());

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setUser(UserLoginResponseUtil.buildUserLogin(userGetByEmail));

        String accessToken = securityUtil.createAccessToken(loginRequest.getUsername(), loginResponse);
        loginResponse.setAccessToken(accessToken);

        return loginResponse;
    }

    @Override
    public LoginResponse refresh(String refreshTokenOld) {
        Jwt jwtChecked = securityUtil.validateRefreshToken(refreshTokenOld);
        String email = jwtChecked.getSubject();
        User userGetByEmailAndRefreshToken = userService.getUserByEmailAndRefreshToken(email, refreshTokenOld);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setUser(UserLoginResponseUtil.buildUserLogin(userGetByEmailAndRefreshToken));

        String accessTokenNew = securityUtil.createAccessToken(email, loginResponse);
        loginResponse.setAccessToken(accessTokenNew);

        return loginResponse;
    }

    @Override
    public LoginResponse.UserGetAccountResponse account() {
        String email = SecurityUtil.getCurrentUserLoginEmail().orElse(null);
        User userGetByEmail = userService.getUserByEmail(email);

        LoginResponse.UserGetAccountResponse userGetAccountResponse = new LoginResponse.UserGetAccountResponse();
        userGetAccountResponse.setUser(UserLoginResponseUtil.buildUserLogin(userGetByEmail));
        return userGetAccountResponse;
    }

    @Override
    public ResponseCookie buildRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(TokenConstant.REFRESH_TOKEN, refreshToken)
                .path("/")
                .secure(true)
                .httpOnly(true)
                .maxAge(refreshTokenExpiration)
                .build();
    }

    @Override
    public ResponseCookie clearRefreshTokenCookie() {
        return ResponseCookie.from(TokenConstant.REFRESH_TOKEN, "")
                .path("/")
                .secure(true)
                .httpOnly(true)
                .maxAge(0)
                .build();
    }

    @Override
    public String generateAndSaveRefreshToken(String email, LoginResponse loginResponse) {
        String refreshToken = securityUtil.createRefreshToken(email, loginResponse);
        userService.saveUserRefreshToken(email, refreshToken);
        return refreshToken;
    }

    @Override
    public void logout() {
        SecurityUtil.getCurrentUserLoginEmail()
                .ifPresent(email -> userService.saveUserRefreshToken(email, null));
    }
}
