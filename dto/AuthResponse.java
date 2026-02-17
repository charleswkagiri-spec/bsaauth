package com.tangazoletu.spotcashesb.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    private String token;
    private String responseCode;
    private String responseMessage;

    public static AuthResponse success(String token) {
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setResponseCode(ResponseCodes.SUCCESS);
        response.setResponseMessage("Authenticated successfully");
        return response;
    }

    public static AuthResponse failure(String message) {
        AuthResponse response = new AuthResponse();
        response.setResponseCode(ResponseCodes.FAILED);
        response.setResponseMessage(message);
        return response;
    }

    public static class ResponseCodes {
        public static final String SUCCESS = "00";
        public static final String FAILED  = "01";

        private ResponseCodes() {}
    }
}
