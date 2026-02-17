package com.tangazoletu.spotcashesb.service;

import com.tangazoletu.spotcashesb.dto.ApiUserResponse;
import com.tangazoletu.spotcashesb.dto.CreateApiUserRequest;
import com.tangazoletu.spotcashesb.entity.ApiUser;

import java.util.List;

public interface ApiUserService {

    ApiUser getUserByUsername(String username);

    ApiUserResponse getUserById(Long id);

    ApiUserResponse createUser(CreateApiUserRequest request);

    ApiUserResponse updateUser(Long id, CreateApiUserRequest request);

    void deleteUser(Long id);

    List<ApiUserResponse> getAllActiveUsers();

    void grantPermissions(Long userId, List<Long> configIds);

    void revokePermissions(Long userId, List<Long> configIds);

    boolean hasAccessToFunction(Long userId, String functionName);
}