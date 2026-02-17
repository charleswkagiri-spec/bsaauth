package com.tangazoletu.spotcashesb.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.tangazoletu.spotcashesb.configuration.security.JwtProperties;
import com.tangazoletu.spotcashesb.entity.ApiUser;
import com.tangazoletu.spotcashesb.security.userdetails.CustomUserDetailsService;
import com.tangazoletu.spotcashesb.security.userdetails.SecurityUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Date;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;
    private final CustomUserDetailsService userDetailsService;

    // Lazily initialised once - algorithm doesn't change
    private JWTVerifier jwtVerifier;

    public boolean hasAuthorizationBearer(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return !ObjectUtils.isEmpty(header) && header.startsWith("Bearer ");
    }

    public String getJwtToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return header.substring(7).trim(); // Strip "Bearer "
    }

    public String generateToken(ApiUser apiUser) {
        try {
            return JWT.create()
                    .withIssuer(jwtProperties.getIssuer())        // ✅ From config - consistent
                    .withSubject(apiUser.getUsername())
                    .withIssuedAt(new Date())
                    .withClaim("app", apiUser.getApplicationName())
                    .withExpiresAt(new Date(System.currentTimeMillis()
                            + (jwtProperties.getAccessTokenLifetime() * 1000)))
                    .sign(Algorithm.HMAC512(jwtProperties.getSecretKey()));

        } catch (JWTCreationException e) {
            log.error("Error generating JWT token for user '{}': {}",
                    apiUser.getUsername(), e.getMessage());
            throw new JWTCreationException("Failed to generate token", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            getJwtVerifier().verify(token);
            return true;
        } catch (JWTVerificationException e) {
            log.warn("Invalid token for subject '{}': {}",
                    safeGetSubject(token), e.getMessage());
            return false;
        }
    }

    /**
     * Validates token but allows a grace period for recently expired tokens.
     * Useful for the refresh endpoint where the token may have just expired.
     */
    public boolean validateTokenWithGrace(String token) {
        try {
            DecodedJWT decoded = JWT.decode(token);
            long gracePeriodMs = jwtProperties.getRefreshGracePeriodSeconds() * 1000L;
            long expiryWithGrace = decoded.getExpiresAt().getTime() + gracePeriodMs;

            if (System.currentTimeMillis() > expiryWithGrace) {
                log.warn("Token too expired to refresh for subject '{}'", decoded.getSubject());
                return false;
            }

            // Also verify signature
            Algorithm algorithm = Algorithm.HMAC512(jwtProperties.getSecretKey());
            JWT.require(algorithm).withIssuer(jwtProperties.getIssuer())
                    .acceptExpiresAt(jwtProperties.getRefreshGracePeriodSeconds())
                    .build()
                    .verify(token);

            return true;
        } catch (Exception e) {
            log.warn("Token grace validation failed: {}", e.getMessage());
            return false;
        }
    }

    public DecodedJWT decodeJwt(String token) {
        return getJwtVerifier().verify(token);
    }

    public String getSubject(String token) {
        return decodeJwt(token).getSubject();
    }

    public SecurityUser getUserFromToken(String token) {
        String username = getSubject(token);
        return (SecurityUser) userDetailsService.loadUserByUsername(username);
    }

    private JWTVerifier getJwtVerifier() {
        if (jwtVerifier == null) {
            jwtVerifier = JWT.require(Algorithm.HMAC512(jwtProperties.getSecretKey()))
                    .withIssuer(jwtProperties.getIssuer())   // ✅ Same issuer as generateToken
                    .build();
        }
        return jwtVerifier;
    }

    private String safeGetSubject(String token) {
        try {
            return JWT.decode(token).getSubject();
        } catch (Exception e) {
            return "unknown";
        }
    }
}