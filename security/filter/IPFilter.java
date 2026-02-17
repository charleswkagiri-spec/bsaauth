package com.tangazoletu.spotcashesb.security.filter;

import com.tangazoletu.spotcashesb.entity.ApiUser;
import com.tangazoletu.spotcashesb.security.userdetails.SecurityUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Validates that the request originates from an IP address whitelisted
 * for the authenticated user. This filter MUST run after JwtTokenFilter
 * because different API clients require different IP restrictions.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IPFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            ApiUser apiUser = securityUser.getApiUser();

            String clientIp = getClientIpAddress(request);

            // Check if IP is whitelisted
            if (!isIpWhitelisted(apiUser, clientIp)) {
                log.error("IP WHITELIST VIOLATION - User: {}, Application: {}, IP: {}",
                        apiUser.getUsername(),
                        apiUser.getApplicationName(),
                        clientIp);

                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Access denied: IP address not whitelisted"
                );
                return;
            }

            log.debug("IP validation successful - User: {}, IP: {}",
                    apiUser.getUsername(), clientIp);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract real client IP address from request.
     * Checks headers in order:
     * 1. X-Forwarded-For (first IP in list)
     * 2. X-Real-IP
     * 3. request.getRemoteAddr() (fallback)
     */
    private String getClientIpAddress(HttpServletRequest request) {
        // Check X-Forwarded-For header (used by most load balancers)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            // Take the first IP in the chain (original client)
            String[] ips = xForwardedFor.split(",");
            String clientIp = ips[0].trim();
            log.debug("Client IP from X-Forwarded-For: {}", clientIp);
            return clientIp;
        }

        // Check X-Real-IP header
        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            log.debug("Client IP from X-Real-IP: {}", xRealIp);
            return xRealIp.trim();
        }

        // Fallback to remote address
        String remoteAddr = request.getRemoteAddr();
        log.debug("Client IP from RemoteAddr: {}", remoteAddr);
        return remoteAddr;
    }

    private boolean isIpWhitelisted(ApiUser apiUser, String clientIp) {
        List<String> whitelistedIps = apiUser.getWhitelistedIps();

        // No whitelist configured = allow all
        if (whitelistedIps == null || whitelistedIps.isEmpty()) {
            log.debug("No IP whitelist configured for user: {}, allowing all IPs",
                    apiUser.getUsername());
            return true;
        }

        // Check if client IP matches any whitelisted IP
        boolean isWhitelisted = whitelistedIps.stream()
                .map(String::trim)
                .anyMatch(whitelistedIp -> matchesIp(clientIp, whitelistedIp));

        return isWhitelisted;
    }

    /**
     * Match client IP against whitelist pattern.
     * Supports:
     * - Exact match: 192.168.1.100
     * - Wildcard: 192.168.1.*
     */
    private boolean matchesIp(String clientIp, String pattern) {
        // Exact match
        if (clientIp.equals(pattern)) {
            return true;
        }

        if (pattern.contains("*")) {
            String regex = pattern.replace(".", "\\.")
                    .replace("*", ".*");
            return clientIp.matches(regex);
        }

        return false;
    }
}
