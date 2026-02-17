package com.tangazoletu.spotcashesb.repositories;

import com.tangazoletu.spotcashesb.entity.ApiUserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiUserPermissionRepository extends JpaRepository<ApiUserPermission, Long> {

    List<ApiUserPermission> findByApiUserId(Long apiUserId);

    List<ApiUserPermission> findByApiConfigId(Long apiConfigId);

    Optional<ApiUserPermission> findByApiUserIdAndApiConfigId(Long apiUserId, Long apiConfigId);

    boolean existsByApiUserIdAndApiConfigId(Long apiUserId, Long apiConfigId);

    // Get all config IDs for a user
    @Query("SELECT p.apiConfigId FROM ApiUserPermission p WHERE p.apiUserId = :userId")
    List<Long> findConfigIdsByUserId(@Param("userId") Long userId);

    // Get all user IDs for a config
    @Query("SELECT p.apiUserId FROM ApiUserPermission p WHERE p.apiConfigId = :configId")
    List<Long> findUserIdsByConfigId(@Param("configId") Long configId);

    // Batch delete
    @Modifying
    @Query("DELETE FROM ApiUserPermission p WHERE p.apiUserId = :userId")
    void deleteByApiUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM ApiUserPermission p WHERE p.apiConfigId = :configId")
    void deleteByApiConfigId(@Param("configId") Long configId);

    // Check if user has access to specific function
    @Query("""
        SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
        FROM ApiUserPermission p
        JOIN ApiConfig c ON p.apiConfigId = c.id
        WHERE p.apiUserId = :userId 
        AND c.functionName = :functionName
        AND c.status = 1
        """)
    boolean userHasAccessToFunction(@Param("userId") Long userId,
                                    @Param("functionName") String functionName);

    // Bulk insert helper (for batch operations)
    @Query(value = """
        INSERT INTO API_USER_PERMISSION (API_USER_ID, API_CONFIG_ID)
        SELECT :userId, c.ID FROM API_CONFIGS c WHERE c.ID IN :configIds
        """, nativeQuery = true)
    @Modifying
    void grantBatchPermissions(@Param("userId") Long userId,
                               @Param("configIds") List<Long> configIds);
}