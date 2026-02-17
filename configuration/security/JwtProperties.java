package com.tangazoletu.spotcashesb.configuration.security;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Validated
public class JwtProperties {

    @NotBlank(message = "jwt.secret-key must be set in application properties")
    private String secretKey;

    // Must match in BOTH generateToken and getJwtVerifier - single source of truth
    private String issuer = "SpotcashEsb";

    @Min(value = 300, message = "Token lifetime must be at least 5 minutes")
    private long accessTokenLifetime = 3600;    // seconds (default: 1 hour)

    // How long after expiry a token can still be refreshed (default: 5 min grace)
    private long refreshGracePeriodSeconds = 300;
}
