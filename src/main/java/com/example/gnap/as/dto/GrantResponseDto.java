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
 * DTO for grant response in the GNAP protocol.
 * Based on the GNAP specification: https://datatracker.ietf.org/doc/html/draft-ietf-gnap-core-protocol
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GrantResponseDto {

    /**
     * The identifier for this grant.
     */
    @JsonProperty("instance_id")
    private String instanceId;

    /**
     * Information about how to continue the interaction.
     */
    private ContinueDto continue_;

    /**
     * Information about how the user can interact with the AS.
     */
    private InteractDto interact;

    /**
     * The access tokens issued in this response.
     */
    private List<AccessTokenDto> access_token;

    /**
     * Subject information.
     */
    private SubjectDto subject;

    /**
     * Client-specific state.
     */
    private Map<String, Object> state;

    /**
     * DTO for continuation information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ContinueDto {
        /**
         * The URI to use for continuation.
         */
        private String uri;

        /**
         * The access token to use for continuation.
         */
        private String access_token;

        /**
         * The wait time in seconds before using the continuation URI.
         */
        private Integer wait;
    }

    /**
     * DTO for interaction information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InteractDto {
        /**
         * The URI to redirect the user to.
         */
        private String redirect;

        /**
         * The app URI to launch.
         */
        private String app;

        /**
         * The user code to display to the user.
         */
        @JsonProperty("user_code")
        private UserCodeDto userCode;

        /**
         * The URI to finish the interaction.
         */
        private FinishDto finish;
    }

    /**
     * DTO for user code information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserCodeDto {
        /**
         * The user code to display to the user.
         */
        private String code;

        /**
         * The URI where the user can enter the code.
         */
        private String uri;
    }

    /**
     * DTO for finish information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FinishDto {
        /**
         * The URI to use for finishing the interaction.
         */
        private String uri;

        /**
         * The method to use for finishing the interaction.
         */
        private String method;
    }

    /**
     * DTO for access token information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AccessTokenDto {
        /**
         * The value of the access token.
         */
        private String value;

        /**
         * The index of the access request this token fulfills.
         */
        private Integer index;

        /**
         * The label of the access token.
         */
        private String label;

        /**
         * The access rights granted by this token.
         */
        private List<GrantRequestDto.AccessDto> access;

        /**
         * The time in seconds until the token expires.
         */
        private Integer expires_in;

        /**
         * The parameters for the token.
         */
        private Map<String, Object> parameters;
    }

    /**
     * DTO for subject information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SubjectDto {
        /**
         * The subject identifier.
         */
        @JsonProperty("sub_ids")
        private List<SubjectIdentifierDto> subjectIdentifiers;

        /**
         * Additional assertions about the subject.
         */
        private Map<String, Object> assertions;

        /**
         * The updated subject.
         */
        private Boolean updated;
    }

    /**
     * DTO for subject identifier information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SubjectIdentifierDto {
        /**
         * The format of the subject identifier.
         */
        private String format;

        /**
         * The subject identifier.
         */
        private String id;
    }
}