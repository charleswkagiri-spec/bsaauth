package com.tangazoletu.spotcashesb.security.filter;

import com.tangazoletu.spotcashesb.entity.ApiConfig;
import com.tangazoletu.spotcashesb.repositories.ApiConfigRepository;
import com.tangazoletu.spotcashesb.security.userdetails.SecurityUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApiAuthorizationFilter extends OncePerRequestFilter {

    private final ApiConfigRepository apiConfigRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            Collection<? extends GrantedAuthority> authorities = securityUser.getAuthorities();

            String requestedUrl = request.getRequestURI();

            if (!isAuthorized(authorities, requestedUrl)) {
                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Unauthorized access to the API"
                );
                log.error("{} attempted unauthorized access to {}",
                        securityUser.getApiUser().getApplicationName(),
                        requestedUrl
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAuthorized(Collection<? extends GrantedAuthority> authorities,
                                 String requestedUrl) {
        try {
            String functionName = extractFunctionName(requestedUrl);
            Optional<ApiConfig> config = apiConfigRepository.findActiveByFunctionName(functionName);

            if (config.isEmpty()) {
                log.warn("API function not found: {}", functionName);
                return false;
            }

            Long configId = config.get().getId();
            return authorities.stream()
                    .anyMatch(auth -> auth.getAuthority().equals(String.valueOf(configId)));

        } catch (Exception ex) {
            log.error("Error checking API authorization", ex);
            return false;
        }
    }

    private String extractFunctionName(String requestedUrl) {
        String[] parts = requestedUrl.split("/");
        return parts[parts.length - 1];
    }
}