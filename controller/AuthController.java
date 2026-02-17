package com.tangazoletu.spotcashesb.controller;

import com.tangazoletu.spotcashesb.dto.AuthRequest;
import com.tangazoletu.spotcashesb.dto.AuthResponse;
import com.tangazoletu.spotcashesb.service.AuthenticationService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/esb")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping(
        value = "/authenticate",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AuthResponse> authenticate(
            @Valid @RequestBody AuthRequest request,
            HttpServletResponse httpServletResponse) {

        log.info("Authentication attempt for user: {}", request.getUsername());
        AuthResponse response = authenticationService.authenticate(request, httpServletResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestHeader("Authorization") String bearerToken,
            HttpServletResponse httpServletResponse) {

        log.info("Token refresh requested");
        AuthResponse response = authenticationService.refresh(bearerToken, httpServletResponse);
        return ResponseEntity.ok(response);
    }
}
