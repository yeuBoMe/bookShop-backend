package com.fullstack.bookShop.controller.auth;

import com.fullstack.bookShop.dto.request.auth.LoginRequest;
import com.fullstack.bookShop.dto.response.UserResponse;
import com.fullstack.bookShop.dto.response.auth.LoginResponse;
import com.fullstack.bookShop.mapper.UserMapper;
import com.fullstack.bookShop.model.User;
import com.fullstack.bookShop.service.interfaces.AuthenticationService;
import com.fullstack.bookShop.service.interfaces.UserService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.fullstack.bookShop.util.constant.auth.TokenConstant;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserMapper userMapper;

    private final UserService userService;

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    @ApiMessage(ActionConstant.LOGIN)
    public ResponseEntity<LoginResponse> loginRequest(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authenticationService.login(loginRequest);

        String refreshToken = authenticationService.generateAndSaveRefreshToken(
                loginRequest.getUsername(),
                loginResponse
        );

        ResponseCookie responseCookie = authenticationService.buildRefreshTokenCookie(refreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(loginResponse);
    }

    @GetMapping("/login/google")
    public void googleLoginRequest(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }

    @PostMapping("/register")
    @ApiMessage(ActionConstant.REGISTER)
    public ResponseEntity<UserResponse> registerRequest(@Valid @RequestBody User userRequest) {
        User userCreated = userService.createUser(userRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userMapper.toResponse(userCreated, ActionConstant.CREATE));
    }

    @PostMapping("/logout")
    @ApiMessage(ActionConstant.LOGOUT)
    public ResponseEntity<Void> logoutRequest() {
        authenticationService.logout();
        ResponseCookie responseCookie = authenticationService.clearRefreshTokenCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(null);
    }

    @GetMapping("/refresh")
    @ApiMessage(ActionConstant.REFRESH)
    public ResponseEntity<LoginResponse> getRefreshTokenRequest(
            @CookieValue(TokenConstant.REFRESH_TOKEN) String refreshTokenFromCookie
    ) {
        LoginResponse loginResponse = authenticationService.refresh(refreshTokenFromCookie);

        String refreshTokenNew = authenticationService.generateAndSaveRefreshToken(
                loginResponse.getUser().getEmail(),
                loginResponse
        );

        ResponseCookie responseCookie = authenticationService.buildRefreshTokenCookie(refreshTokenNew);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(loginResponse);
    }

    @GetMapping("/account")
    @ApiMessage(ActionConstant.ACCOUNT)
    public ResponseEntity<LoginResponse.UserGetAccountResponse> accountInfoRequest() {
        return ResponseEntity.ok(authenticationService.account());
    }
}
