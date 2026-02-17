package com.tangazoletu.spotcashesb.security.filter;

import com.tangazoletu.spotcashesb.security.jwt.JwtUtil;
import com.tangazoletu.spotcashesb.security.userdetails.SecurityUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // No bearer token - let Spring Security handle it (will result in 403)
        if (!jwtUtil.hasAuthorizationBearer(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = jwtUtil.getJwtToken(request);

        // Invalid token - clear context and continue (Spring Security blocks unauthenticated requests)
        if (!jwtUtil.validateToken(token)) {
            log.warn("Invalid token received from {}", request.getRemoteAddr());
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        setAuthenticationContext(token, request);
        filterChain.doFilter(request, response);
    }

    private void setAuthenticationContext(String token, HttpServletRequest request) {
        try {
            SecurityUser userDetails = jwtUtil.getUserFromToken(token);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()  // ✅ CRITICAL - authorities must be set
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Authenticated user: '{}' with {} authorities",
                    userDetails.getUsername(),
                    userDetails.getAuthorities().size());

        } catch (Exception e) {
            log.error("Failed to set authentication context: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
    }
}
