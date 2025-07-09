package com.example.gnap.as.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for token introspection in the GNAP protocol.
 * Based on the GNAP specification: https://datatracker.ietf.org/doc/html/draft-ietf-gnap-core-protocol
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenIntrospectionDto {

    /**
     * Whether the token is active.
     */
    private Boolean active;

    /**
     * The access rights granted by this token.
     */
    private List<GrantRequestDto.AccessDto> access;

    /**
     * The subject of the token.
     */
    private GrantResponseDto.SubjectDto subject;

    /**
     * The time in seconds until the token expires.
     */
    @JsonProperty("expires_in")
    private Integer expiresIn;

    /**
     * The identifier of the grant that issued this token.
     */
    @JsonProperty("grant_id")
    private String grantId;

    /**
     * The identifier of the client that requested this token.
     */
    @JsonProperty("client_id")
    private String clientId;

    /**
     * The time at which the token was issued, in seconds since the epoch.
     */
    @JsonProperty("iat")
    private Long issuedAt;

    /**
     * Additional parameters for the token.
     */
    private Map<String, Object> parameters;
}