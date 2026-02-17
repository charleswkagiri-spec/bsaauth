package com.tangazoletu.spotcashesb.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class GrantPermissionsRequest {

    @NotEmpty(message = "At least one config ID is required")
    private List<Long> configIds;
}
