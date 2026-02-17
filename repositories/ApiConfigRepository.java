package com.tangazoletu.spotcashesb.repositories;

import com.tangazoletu.spotcashesb.entity.ApiConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiConfigRepository extends JpaRepository<ApiConfig, Long> {

    Optional<ApiConfig> findByFunctionName(String functionName);

    List<ApiConfig> findByStatus(Integer status);

    List<ApiConfig> findByRequestType(String requestType);

    boolean existsByFunctionName(String functionName);

    @Query("SELECT c FROM ApiConfig c WHERE c.status = 1")
    List<ApiConfig> findAllActive();

    @Query("SELECT c FROM ApiConfig c WHERE c.id IN :ids")
    List<ApiConfig> findByIdIn(@Param("ids") List<Long> ids);

    @Query("""
        SELECT DISTINCT c FROM ApiConfig c
        JOIN ApiUserPermission p ON p.apiConfigId = c.id
        WHERE p.apiUserId = :userId AND c.status = 1
        """)
    List<ApiConfig> findAccessibleConfigsForUser(@Param("userId") Long userId);

    // Find by function name and ensure active
    @Query("SELECT c FROM ApiConfig c WHERE c.functionName = :functionName AND c.status = 1")
    Optional<ApiConfig> findActiveByFunctionName(@Param("functionName") String functionName);
}
