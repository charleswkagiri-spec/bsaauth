package com.tangazoletu.spotcashesb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "API_USER_PERMISSION",
        uniqueConstraints = @UniqueConstraint(columnNames = {"API_USER_ID", "API_CONFIG_ID"})
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiUserPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "API_USER_PERMISSION_SEQ")
    @SequenceGenerator(name = "API_USER_PERMISSION_SEQ", sequenceName = "API_USER_PERMISSION_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "API_USER_ID", nullable = false)
    private Long apiUserId;

    @Column(name = "API_CONFIG_ID", nullable = false)
    private Long apiConfigId;
}
