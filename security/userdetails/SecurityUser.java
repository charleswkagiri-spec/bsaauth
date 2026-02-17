package com.tangazoletu.spotcashesb.security.userdetails;

import com.tangazoletu.spotcashesb.entity.ApiUser;
import com.tangazoletu.spotcashesb.entity.enums.ApiUserStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class SecurityUser implements UserDetails {

    private final ApiUser apiUser;
    private final List<Long> allowedConfigIds;  // Passed in from CustomUserDetailsService

    public SecurityUser(ApiUser apiUser, List<Long> allowedConfigIds) {
        this.apiUser = apiUser;
        this.allowedConfigIds = allowedConfigIds;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Each authority is a config ID the user is allowed to call
        // ApiAuthorizationFilter will check these against the requested function's config ID
        return allowedConfigIds.stream()
                .map(configId -> new SimpleGrantedAuthority(String.valueOf(configId)))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return apiUser.getPassword();
    }

    @Override
    public String getUsername() {
        return apiUser.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // No expiry concept on accounts currently
    }

    @Override
    public boolean isAccountNonLocked() {
        return apiUser.getStatus() != ApiUserStatus.LOCKED
                && apiUser.getStatus() != ApiUserStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // Credential expiry not required for now
    }

    @Override
    public boolean isEnabled() {
        return apiUser.getStatus() == ApiUserStatus.ACTIVE;
    }
}