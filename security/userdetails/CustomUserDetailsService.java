package com.tangazoletu.spotcashesb.security.userdetails;

import com.tangazoletu.spotcashesb.entity.ApiUser;
import com.tangazoletu.spotcashesb.repositories.ApiUserPermissionRepository;
import com.tangazoletu.spotcashesb.repositories.ApiUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final ApiUserRepository apiUserRepository;
    private final ApiUserPermissionRepository permissionRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user: '{}'", username);

        ApiUser apiUser = apiUserRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found: '{}'", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        // Fetch config IDs the user is permitted to call
        // These become the user's GrantedAuthorities
        List<Long> allowedConfigIds = permissionRepository.findConfigIdsByUserId(apiUser.getId());

        log.debug("Loaded user '{}' with {} permitted APIs",
                username, allowedConfigIds.size());

        return new SecurityUser(apiUser, allowedConfigIds);
    }
}
