package com.fullstack.bookShop.controller.auth;

import com.fullstack.bookShop.domain.dto.auth.LoginDTO;
import com.fullstack.bookShop.domain.dto.auth.RestLoginDTO;
import com.fullstack.bookShop.domain.dto.user.RestUserCreateDTO;
import com.fullstack.bookShop.domain.entity.Permission;
import com.fullstack.bookShop.domain.entity.User;
import com.fullstack.bookShop.service.UserService;
import com.fullstack.bookShop.util.annotation.ApiMessage;
import com.fullstack.bookShop.util.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    @Value("${bookshop.jwt.refresh-token-validity-in-seconds}")
    private Long refreshTokenExpiration;

    private final UserService userService;

    private final SecurityUtil securityUtil;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    @ApiMessage("Login successfully")
    @PostMapping("/login")
    public ResponseEntity<RestLoginDTO> loginRequest(@Valid @RequestBody LoginDTO loginDTO) {
        // class contains login info not validate
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
                = new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());

        /* AuthenticationManagerBuilder calls AuthenticationManager to validate login info
         * then AuthenticationManager will use func loadUserByUsername in CustomUserDetailsService to
         * compare duplicated data before validate right/wrong login request
         */
        Authentication authentication = authenticationManagerBuilder.getObject()
                .authenticate(usernamePasswordAuthenticationToken);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User userGetByEmail = userService.getUserByEmail(loginDTO.getUsername());
        RestLoginDTO restLoginDTO = new RestLoginDTO();
        restLoginDTO.setUser(buildUserLogin(userGetByEmail));

        String accessToken = securityUtil.createAccessToken(loginDTO.getUsername(), restLoginDTO);
        restLoginDTO.setAccessToken(accessToken);

        String refreshToken = securityUtil.createRefreshToken(loginDTO.getUsername(), restLoginDTO);
        userService.handleSaveUserRefreshToken(loginDTO.getUsername(), refreshToken);

        ResponseCookie responseCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .path("/")
                .secure(true)
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(restLoginDTO);
    }

    @ApiMessage("Register successfully")
    @PostMapping("/register")
    public ResponseEntity<RestUserCreateDTO> registerRequest(@Valid @RequestBody User userRequest) {
        User userCreated = userService.createUser(userRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.convertUserToRestUserCreateDTO(userCreated));
    }

    @ApiMessage("Get account info")
    @GetMapping("/account")
    public ResponseEntity<RestLoginDTO.UserGetAccount> getAccountRequest() {
        String email = SecurityUtil.getCurrentUserLoginEmail().orElse(null);
        User userGetByEmail = userService.getUserByEmail(email);

        RestLoginDTO.UserGetAccount userGetAccount = new RestLoginDTO.UserGetAccount();
        userGetAccount.setUser(buildUserLogin(userGetByEmail));

        return ResponseEntity.ok(userGetAccount);
    }

    @ApiMessage("Get user by refresh token ")
    @GetMapping("/refresh")
    public ResponseEntity<RestLoginDTO> getRefreshTokenRequest(
            @CookieValue("refresh_token") String refreshTokenFromCookie
    ) {
        Jwt jwtChecked = securityUtil.checkValidRefreshToken(refreshTokenFromCookie);
        String emailGetFromJwt = jwtChecked.getSubject();

        User userGetByEmailAndRefreshToken =
                userService.getUserByEmailAndRefreshToken(emailGetFromJwt, refreshTokenFromCookie);

        RestLoginDTO restLoginDTO = new RestLoginDTO();
        restLoginDTO.setUser(buildUserLogin(userGetByEmailAndRefreshToken));

        String accessTokenNew = securityUtil.createAccessToken(emailGetFromJwt, restLoginDTO);
        restLoginDTO.setAccessToken(accessTokenNew);

        String refreshTokenNew = securityUtil.createRefreshToken(emailGetFromJwt, restLoginDTO);
        userService.handleSaveUserRefreshToken(emailGetFromJwt, refreshTokenNew);

        ResponseCookie responseCookie = ResponseCookie.from("refresh_token", refreshTokenNew)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(restLoginDTO);
    }

    @ApiMessage("Logout successfully")
    @PostMapping("/logout")
    public ResponseEntity<Void> logoutRequest() {
        String email = SecurityUtil.getCurrentUserLoginEmail().orElse(null);
        userService.handleSaveUserRefreshToken(email, null);

        ResponseCookie responseCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(null);
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
