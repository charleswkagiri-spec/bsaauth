package com.tangazoletu.spotcashesb.service;

import com.tangazoletu.spotcashesb.dto.AuthRequest;
import com.tangazoletu.spotcashesb.dto.AuthResponse;
import com.tangazoletu.spotcashesb.security.jwt.JwtUtil;
import com.tangazoletu.spotcashesb.security.userdetails.SecurityUser;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;

    public AuthResponse authenticate(AuthRequest request, HttpServletResponse httpResponse) {
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();

            String token = jwtUtil.generateToken(securityUser.getApiUser());

            log.info("User '{}' authenticated successfully", securityUser.getUsername());

            return AuthResponse.success(token);

        } catch (UsernameNotFoundException | BadCredentialsException e) {
            // Don't reveal whether username exists
            log.warn("Failed login attempt for username: '{}'", request.getUsername());
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return AuthResponse.failure("Invalid username or password");

        } catch (DisabledException e) {
            log.warn("Disabled account login attempt: '{}'", request.getUsername());
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return AuthResponse.failure("Your API credentials have been deactivated");

        } catch (Exception e) {
            log.error("Unexpected error during authentication for '{}': {}",
                    request.getUsername(), e.getMessage(), e);
            httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return AuthResponse.failure("An unexpected error occurred. Please try again later");
        }
    }

    public AuthResponse refresh(String bearerToken, HttpServletResponse httpResponse) {
        try {
            // Strip "Bearer " prefix
            String token = bearerToken.startsWith("Bearer ")
                           ? bearerToken.substring(7)
                           : bearerToken;

            // Validate token (allow slightly expired with grace period - see JwtUtil)
            if (!jwtUtil.validateTokenWithGrace(token)) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return AuthResponse.failure("Token is invalid or too expired to refresh");
            }

            String username = jwtUtil.getSubject(token);

            // Re-load user to get fresh permissions/status
            SecurityUser securityUser = jwtUtil.getUserFromToken(token);

            if (!securityUser.isEnabled()) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return AuthResponse.failure("Account has been deactivated");
            }

            String newToken = jwtUtil.generateToken(securityUser.getApiUser());

            log.info("Token refreshed for user '{}'", username);

            return AuthResponse.success(newToken);

        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return AuthResponse.failure("Token refresh failed");
        }
    }
}