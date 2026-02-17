package com.tangazoletu.spotcashesb.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimpleApiResponse {
    private String responseCode;
    private String responseMessage;
    private Object data;

    public static SimpleApiResponse success(String message) {
        SimpleApiResponse response = new SimpleApiResponse();
        response.setResponseCode("00");
        response.setResponseMessage(message);
        return response;
    }

    public static SimpleApiResponse success(String message, Object data) {
        SimpleApiResponse response = success(message);
        response.setData(data);
        return response;
    }

    public static SimpleApiResponse failure(String message) {
        SimpleApiResponse response = new SimpleApiResponse();
        response.setResponseCode("01");
        response.setResponseMessage(message);
        return response;
    }
}
