package com.tangazoletu.spotcashesb.repositories;

import com.tangazoletu.spotcashesb.entity.ApiUser;
import com.tangazoletu.spotcashesb.entity.enums.ApiUserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApiUserRepository extends JpaRepository<ApiUser, Long> {
    Optional<ApiUser> findByUsername(String username);

    boolean existsByUsername(String username);

    List<ApiUser> findByStatus(ApiUserStatus status);

    List<ApiUser> findByApplicationName(String applicationName);

    @Query("SELECT u FROM ApiUser u WHERE u.status = :status AND u.dateCreated >= :since")
    List<ApiUser> findActiveUsersSince(@Param("status") ApiUserStatus status,
                                       @Param("since") LocalDateTime since);

    @Query("SELECT u FROM ApiUser u LEFT JOIN FETCH u.permissions WHERE u.username = :username")
    Optional<ApiUser> findByUsernameWithPermissions(@Param("username") String username);

    @Query("SELECT u FROM ApiUser u WHERE u.status IN :statuses")
    List<ApiUser> findByStatusIn(@Param("statuses") List<ApiUserStatus> statuses);

    @Query(value = """
        SELECT u.* FROM API_USER u
        WHERE u.STATUS = 'ACTIVE'
        AND EXISTS (
            SELECT 1 FROM API_USER_PERMISSION p
            WHERE p.API_USER_ID = u.ID
            AND p.API_CONFIG_ID = :configId
        )
        """, nativeQuery = true)
    List<ApiUser> findUsersWithAccessToConfig(@Param("configId") Long configId);

    @Query("SELECT u FROM ApiUser u WHERE u.username = :username AND " +
           "(u.whitelistedIps IS NULL OR u.whitelistedIps = '' OR " +
           ":ipAddress MEMBER OF u.whitelistedIps)")
    Optional<ApiUser> findByUsernameAndIpAllowed(@Param("username") String username,
                                                  @Param("ipAddress") String ipAddress);
}
