package com.fullstack.bookShop.util.security;

import com.fullstack.bookShop.dto.auth.RestLoginDTO;
import com.nimbusds.jose.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SecurityUtil {

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;

    @Value("${bookshop.jwt.base64-secret}")
    private String jwtKey;

    @Value("${bookshop.jwt.access-token-validity-in-seconds}")
    private Long accessTokenExpiration;

    @Value("${bookshop.jwt.refresh-token-validity-in-seconds}")
    private Long refreshTokenExpiration;

    private final JwtEncoder jwtEncoder;

    public SecretKey getSecretKey() {
        byte[] bytes = Base64.from(this.jwtKey).decode();
        return new SecretKeySpec(
                bytes,
                0,
                bytes.length,
                JWT_ALGORITHM.getName()
        );
    }

    public Jwt checkValidRefreshToken(String refreshToken) {
        NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(getSecretKey())
                .macAlgorithm(JWT_ALGORITHM)
                .build();

        try {
            return nimbusJwtDecoder.decode(refreshToken);
        } catch (Exception ex) {
            System.out.println(">>> Refresh token error: " + ex.getMessage());
            throw ex;
        }
    }

    public String createAccessToken(String email, RestLoginDTO restLoginDTO) {
        RestLoginDTO.UserInsideToken userInsideToken = new RestLoginDTO.UserInsideToken(
                restLoginDTO.getUser().getId(),
                restLoginDTO.getUser().getName(),
                restLoginDTO.getUser().getEmail()
        );

        Instant now = Instant.now();
        Instant expiration = now.plus(this.accessTokenExpiration, ChronoUnit.SECONDS);

        // payload
        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(expiration)
                .subject(email)
                .claim("user", userInsideToken)
                .build();

        // header
        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();

        return this.jwtEncoder
                .encode(JwtEncoderParameters.from(jwsHeader, jwtClaimsSet))
                .getTokenValue();
    }

    public String createRefreshToken(String email, RestLoginDTO restLoginDTO) {
        RestLoginDTO.UserInsideToken userInsideToken = new RestLoginDTO.UserInsideToken(
                restLoginDTO.getUser().getId(),
                restLoginDTO.getUser().getName(),
                restLoginDTO.getUser().getEmail()
        );

        Instant now = Instant.now();
        Instant expiration = now.plus(this.refreshTokenExpiration, ChronoUnit.SECONDS);

        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(expiration)
                .subject(email)
                .claim("user", userInsideToken)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();

        return this.jwtEncoder
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
        } else if (authentication.getPrincipal() instanceof String s) {
            return s;
        } else if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        return null;
    }
}
