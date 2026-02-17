package com.tangazoletu.spotcashesb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tangazoletu.spotcashesb.converter.IpListConverter;
import com.tangazoletu.spotcashesb.entity.enums.ApiUserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ApiUser {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "API_USER_SEQ")
    @SequenceGenerator(name = "API_USER_SEQ", sequenceName = "API_USER_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "USERNAME", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Username is required")
    private String username;

    @Column(name = "PASSWORD", nullable = false, length = 255)
    @NotBlank(message = "Password is required")
    @JsonIgnore  // Don't serialize password in JSON responses
    private String password;  // Should be encrypted

    @Column(name = "APPLICATION_NAME", length = 100)
    private String applicationName;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ApiUserStatus status = ApiUserStatus.ACTIVE;

    @Column(name = "WHITELISTED_IPS", length = 1000)
    @Convert(converter = IpListConverter.class)
    private List<String> whitelistedIps;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE_CREATED", nullable = false, updatable = false)
    private Date dateCreated;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE_UPDATED")
    private Date dateUpdated;

    @OneToMany(mappedBy = "apiUser", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ApiUserPermission> permissions;

    @PrePersist
    protected void onCreate() {
        if (dateCreated == null) {
            dateCreated = new Date();
        }
        if (dateUpdated == null) {
            dateUpdated = new Date();
        }
        if (status == null) {
            status = ApiUserStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        dateUpdated = new Date();
    }

    public boolean isActive() {
        return status == ApiUserStatus.ACTIVE;
    }

    public boolean isIpWhitelisted(String ip) {
        if (whitelistedIps == null || whitelistedIps.isEmpty()) {
            return false;
        }
        return whitelistedIps.stream()
                .anyMatch(whitelistedIp -> whitelistedIp.trim().equals(ip.trim()));
    }

    public List<Long> getAllowedConfigIds() {
        return permissions.stream()
                .map(ApiUserPermission::getApiConfigId)
                .collect(Collectors.toList());
    }

    public boolean hasAccessToFunction(String functionName, List<ApiConfig> allConfigs) {
        Set<Long> allowedIds = getAllowedConfigIds().stream().collect(Collectors.toSet());
        return allConfigs.stream()
                .filter(config -> allowedIds.contains(config.getId()))
                .anyMatch(config -> config.getFunctionName().equals(functionName));
    }
}