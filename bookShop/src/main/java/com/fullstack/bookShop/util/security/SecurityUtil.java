package com.fullstack.bookShop.util.security;

import com.fullstack.bookShop.dto.response.auth.LoginResponse;
import com.fullstack.bookShop.dto.response.auth.UserInsideTokenResponse;
import com.fullstack.bookShop.dto.response.auth.UserLoginResponse;
import com.fullstack.bookShop.util.constant.auth.AuthorityConstant;
import com.fullstack.bookShop.util.constant.auth.TokenConstant;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SecurityUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityUtil.class);

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;

    @Value("${bookshop.jwt.access-token-validity-in-seconds}")
    private Long accessTokenExpiration;

    @Value("${bookshop.jwt.refresh-token-validity-in-seconds}")
    private Long refreshTokenExpiration;

    private final SecretKey secretKey;

    private final JwtEncoder jwtEncoder;

    public Jwt validateRefreshToken(String refreshToken) {
        NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(JWT_ALGORITHM)
                .build();

        try {
            return nimbusJwtDecoder.decode(refreshToken);
        } catch (Exception ex) {
            LOGGER.error(">>> Refresh Token decode failed: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    public String createAccessToken(String email, LoginResponse loginResponse) {
        return createJwtToken(email, loginResponse, accessTokenExpiration);
    }

    public String createRefreshToken(String email, LoginResponse loginResponse) {
        return createJwtToken(email, loginResponse, refreshTokenExpiration);
    }

    private String createJwtToken(
            String email,
            LoginResponse loginResponse,
            Long expirationSeconds
    ) {
        Instant now = Instant.now();
        Instant expiration = now.plus(expirationSeconds, ChronoUnit.SECONDS);

        UserInsideTokenResponse userInsideTokenResponse = buildUserInsideTokenResponse(loginResponse);
        String scope = buildRoleOfUserLoginInClaim(loginResponse);

        // payload
        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(expiration)
                .subject(email)
                .claim(TokenConstant.USER_INSIDE_TOKEN, userInsideTokenResponse)
                .claim(TokenConstant.SCOPE, scope)
                .build();

        // header
        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();

        return jwtEncoder
                .encode(JwtEncoderParameters.from(jwsHeader, jwtClaimsSet))
                .getTokenValue();
    }

    public static Optional<String> getCurrentUserLoginEmail() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
    }

    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null) {
            return null;
        } else if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
            return springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        } else if (authentication.getPrincipal() instanceof String s) {
            return s;
        }
        return null;
    }

    private UserInsideTokenResponse buildUserInsideTokenResponse(LoginResponse loginResponse) {
        return new UserInsideTokenResponse(
                loginResponse.getUser().getId(),
                loginResponse.getUser().getName(),
                loginResponse.getUser().getEmail()
        );
    }

    private String buildRoleOfUserLoginInClaim(LoginResponse loginResponse) {
        String roleOfUser = Optional.ofNullable(loginResponse.getUser().getRole())
                .map(UserLoginResponse.RoleOfUserLoginResponse::getName)
                .orElse(AuthorityConstant.USER);

        return AuthorityConstant.ROLE_PREFIX + roleOfUser;
    }
}
