package com.tangazoletu.spotcashesb.entity.enums;

import lombok.Getter;

@Getter
public enum ApiUserStatus {
    ACTIVE(1, "Active"),
    INACTIVE(0, "Inactive"),
    SUSPENDED(2, "Suspended"),
    LOCKED(3, "Locked");

    private final int code;
    private final String description;

    ApiUserStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ApiUserStatus fromCode(int code) {
        for (ApiUserStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status code: " + code);
    }
}