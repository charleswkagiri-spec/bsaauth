package com.tangazoletu.spotcashesb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateApiUserRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Application name is required")
    private String applicationName;

    // Optional: comma-separated IPs, empty means allow all
    private List<String> whitelistedIps;

    // Optional: config IDs to grant on creation
    private List<Long> permissionIds;
}