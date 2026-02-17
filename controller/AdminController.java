package com.tangazoletu.spotcashesb.controller;

import com.tangazoletu.spotcashesb.dto.ApiUserResponse;
import com.tangazoletu.spotcashesb.dto.CreateApiUserRequest;
import com.tangazoletu.spotcashesb.dto.GrantPermissionsRequest;
import com.tangazoletu.spotcashesb.dto.SimpleApiResponse;
import com.tangazoletu.spotcashesb.service.ApiUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/esb/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final ApiUserService apiUserService;

    @PostMapping("/users")
    public ResponseEntity<ApiUserResponse> createUser(@Valid @RequestBody CreateApiUserRequest request) {
        log.info("Creating new API user: {}", request.getUsername());
        ApiUserResponse response = apiUserService.createUser(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<List<ApiUserResponse>> getAllActiveUsers() {
        return ResponseEntity.ok(apiUserService.getAllActiveUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiUserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(apiUserService.getUserById(id));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiUserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody CreateApiUserRequest request) {

        log.info("Updating user ID: {}", id);
        ApiUserResponse response = apiUserService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<SimpleApiResponse> deleteUser(@PathVariable Long id) {
        log.info("Deleting user ID: {}", id);
        apiUserService.deleteUser(id);
        return ResponseEntity.ok(SimpleApiResponse.success("User deactivated successfully"));
    }

    // ---------- Permission Management ----------

    @PostMapping("/users/{id}/permissions")
    public ResponseEntity<SimpleApiResponse> grantPermissions(
            @PathVariable Long id,
            @RequestBody GrantPermissionsRequest request) {

        log.info("Granting permissions to user ID: {}", id);
        apiUserService.grantPermissions(id, request.getConfigIds());
        return ResponseEntity.ok(SimpleApiResponse.success("Permissions granted successfully"));
    }

    @DeleteMapping("/users/{id}/permissions")
    public ResponseEntity<SimpleApiResponse> revokePermissions(
            @PathVariable Long id,
            @RequestBody GrantPermissionsRequest request) {

        log.info("Revoking permissions from user ID: {}", id);
        apiUserService.revokePermissions(id, request.getConfigIds());
        return ResponseEntity.ok(SimpleApiResponse.success("Permissions revoked successfully"));
    }
}