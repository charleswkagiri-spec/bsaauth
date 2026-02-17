package com.tangazoletu.spotcashesb.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "API_CONFIGS")
public class ApiConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "API_CONFIGS_SEQ")
    @SequenceGenerator(name = "API_CONFIGS_SEQ", sequenceName = "API_CONFIGS_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    // Func identification
    @Column(name = "FUNCTION_NAME", nullable = false)
    private String functionName;

    // Endpoint config
    @Column(name = "FUNCTION_URL", nullable = false)
    private String functionUrl;

    @Column(name = "PATH_NAME", length = 50)
    private String pathName;

    @Column(name = "REQUEST_TYPE", length = 10, nullable = false) // PROTOCOL: REST, SOAP
    private String requestType;

    @Column(name =  "REST_METHOD", length =  10)  // GET, POST, PUT, DELETE
    private  String restMethod;

    // Status & flags (use Integer for better null handling)
    @Column(name = "STATUS", nullable = false)
    @Min(0) @Max(1)
    private Integer status = 1;  // 1=Active, 0=Inactive

    @Column(name = "USE_MEMBER_NO")
    private Integer useMemberNo = 0;

    // Auth details
    @Column(name = "REQUIRES_AUTH", nullable = false)
    private Integer requiresAuth = 1;  // 1=Yes, 0=No

    @Column(name = "REST_AUTH_TYPE", length = 50)  // BASIC, BEARER
    private String restAuthType;

    @Column(name = "AUTH_USERNAME", length = 100)
    private String authUsername;

    @Column(name = "AUTH_PASSWORD", length = 100)  // Should be encrypted
    private String authPassword;

    @Column(name = "AUTH_DOMAIN", length = 100)
    private String authDomain;

    // Request/Response
    @Column(name = "REQUEST_TEMPLATE", columnDefinition = "CLOB", nullable = false)
    private String requestTemplate;

    @Column(name = "RESPONSE_FORMAT", columnDefinition = "CLOB")
    private String responseFormat;

    @Column(name = "SOAP_BASE_STRING", length = 1000)
    private String soapBaseString;

    // Formatting
    @Column(name = "REQUEST_FORMAT_FUNCTIONS", length = 100)
    private String requestFormatFunctions;

    @Column(name = "FORMAT_RESPONSE", nullable = false)
    private Integer formatResponse = 1;

    @Column(name = "COMMENT")
    private String comment;

    // metadata
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE_CREATED")
    private Date dateCreated;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE_UPDATED")
    private Date dateUpdated;

    @PrePersist
    protected void onCreate() {
        dateCreated = new Date();
        dateUpdated = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        dateUpdated = new Date();
    }
}