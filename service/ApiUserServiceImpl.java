package com.tangazoletu.spotcashesb.service;

import com.tangazoletu.spotcashesb.dto.ApiUserResponse;
import com.tangazoletu.spotcashesb.dto.CreateApiUserRequest;
import com.tangazoletu.spotcashesb.entity.ApiUser;
import com.tangazoletu.spotcashesb.entity.ApiUserPermission;
import com.tangazoletu.spotcashesb.entity.enums.ApiUserStatus;
import com.tangazoletu.spotcashesb.exception.ApiUserNotFoundException;
import com.tangazoletu.spotcashesb.repositories.ApiUserPermissionRepository;
import com.tangazoletu.spotcashesb.repositories.ApiUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApiUserServiceImpl implements ApiUserService {

    private final ApiUserRepository apiUserRepository;
    private final ApiUserPermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ApiUser getUserByUsername(String username) {
        return apiUserRepository.findByUsername(username)
                .orElseThrow(() -> new ApiUserNotFoundException("User not found: " + username));
    }

    @Override
    public ApiUserResponse getUserById(Long id) {
        ApiUser user = apiUserRepository.findById(id)
                .orElseThrow(() -> new ApiUserNotFoundException("User not found with ID: " + id));
        return toUserResponse(user);
    }

    @Override
    @Transactional
    public ApiUserResponse createUser(CreateApiUserRequest request) {
        if (apiUserRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        ApiUser user = ApiUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .applicationName(request.getApplicationName())
                .status(ApiUserStatus.ACTIVE)
                .whitelistedIps(request.getWhitelistedIps())
                .build();

        ApiUser saved = apiUserRepository.save(user);
        log.info("Created API user: '{}' for application: '{}'",
                saved.getUsername(), saved.getApplicationName());

        // Grant any initial permissions if provided
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            grantPermissions(saved.getId(), request.getPermissionIds());
        }

        return toUserResponse(saved);
    }

    @Override
    @Transactional
    public ApiUserResponse updateUser(Long id, CreateApiUserRequest request) {
        ApiUser user = apiUserRepository.findById(id)
                .orElseThrow(() -> new ApiUserNotFoundException("User not found with ID: " + id));

        user.setApplicationName(request.getApplicationName());
        user.setWhitelistedIps(request.getWhitelistedIps());

        // Only update password if explicitly provided
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            log.info("Password updated for user: '{}'", user.getUsername());
        }

        ApiUser updated = apiUserRepository.save(user);
        log.info("Updated user: '{}'", updated.getUsername());

        return toUserResponse(updated);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        ApiUser user = apiUserRepository.findById(id)
                .orElseThrow(() -> new ApiUserNotFoundException("User not found with ID: " + id));

        // Soft delete - keep record but deactivate
        user.setStatus(ApiUserStatus.INACTIVE);
        apiUserRepository.save(user);

        log.info("Deactivated user: '{}' (ID: {})", user.getUsername(), id);
    }

    @Override
    public List<ApiUserResponse> getAllActiveUsers() {
        return apiUserRepository.findByStatus(ApiUserStatus.ACTIVE)
                .stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void grantPermissions(Long userId, List<Long> configIds) {
        apiUserRepository.findById(userId)
                .orElseThrow(() -> new ApiUserNotFoundException("User not found with ID: " + userId));

        // Only insert permissions that don't already exist
        List<ApiUserPermission> newPermissions = configIds.stream()
                .filter(configId -> !permissionRepository
                        .existsByApiUserIdAndApiConfigId(userId, configId))
                .map(configId -> ApiUserPermission.builder()
                        .apiUserId(userId)
                        .apiConfigId(configId)
                        .build())
                .collect(Collectors.toList());

        if (!newPermissions.isEmpty()) {
            permissionRepository.saveAll(newPermissions);
            log.info("Granted {} permissions to user ID: {}", newPermissions.size(), userId);
        } else {
            log.info("No new permissions to grant for user ID: {} (all already exist)", userId);
        }
    }

    @Override
    @Transactional
    public void revokePermissions(Long userId, List<Long> configIds) {
        configIds.forEach(configId ->
                permissionRepository
                        .findByApiUserIdAndApiConfigId(userId, configId)
                        .ifPresent(permission -> {
                            permissionRepository.delete(permission);
                            log.info("Revoked permission - User: {}, Config: {}", userId, configId);
                        })
        );
    }

    @Override
    public boolean hasAccessToFunction(Long userId, String functionName) {
        return permissionRepository.userHasAccessToFunction(userId, functionName);
    }

    private ApiUserResponse toUserResponse(ApiUser user) {
        return ApiUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .applicationName(user.getApplicationName())
                .status(user.getStatus())
                .whitelistedIps(user.getWhitelistedIps())
                .dateCreated(user.getDateCreated())
                .dateUpdated(user.getDateUpdated())
                .build();
    }
}