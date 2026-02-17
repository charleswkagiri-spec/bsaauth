package com.tangazoletu.spotcashesb.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tangazoletu.spotcashesb.entity.enums.ApiUserStatus;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiUserResponse {

    private Long id;
    private String username;
    private String applicationName;
    private ApiUserStatus status;
    private List<String> whitelistedIps;
    private Date dateCreated;
    private Date dateUpdated;

    // Note: password is NEVER included here
}