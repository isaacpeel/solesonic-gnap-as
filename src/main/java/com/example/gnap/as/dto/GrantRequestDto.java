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
 * DTO for grant request in the GNAP protocol.
 * Based on the GNAP specification: https://datatracker.ietf.org/doc/html/draft-ietf-gnap-core-protocol
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GrantRequestDto {

    /**
     * The client instance making the request.
     */
    private ClientDto client;

    /**
     * The access being requested.
     */
    private List<AccessDto> access;

    /**
     * The subject of the request.
     */
    private SubjectDto subject;

    /**
     * The user interaction modes supported by the client.
     */
    @JsonProperty("interact")
    private InteractDto interact;

    /**
     * The client's capabilities and preferences for returning information.
     */
    private List<String> capabilities;

    /**
     * Client-specific state.
     */
    private Map<String, Object> state;

    /**
     * DTO for client information in a grant request.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ClientDto {
        /**
         * The client's instance identifier.
         */
        @JsonProperty("instance_id")
        private String instanceId;

        /**
         * The key the client is using to sign the request.
         */
        private KeyDto key;

        /**
         * The client's display information.
         */
        private DisplayDto display;
    }

    /**
     * DTO for access request in a grant request.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AccessDto {
        /**
         * The type of access being requested.
         */
        private String type;

        /**
         * The actions being requested.
         */
        private List<String> actions;

        /**
         * The locations being requested.
         */
        private List<String> locations;

        /**
         * The data types being requested.
         */
        @JsonProperty("datatypes")
        private List<String> dataTypes;

        /**
         * The identifier of the resource server.
         */
        @JsonProperty("resource_server")
        private String resourceServer;
    }

    /**
     * DTO for subject information in a grant request.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SubjectDto {
        /**
         * The formats the client can accept for the subject.
         */
        private List<String> formats;

        /**
         * The assertions the client can present about the subject.
         */
        private Map<String, Object> assertions;
    }

    /**
     * DTO for interaction modes in a grant request.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InteractDto {
        /**
         * The redirect URI for the client.
         */
        private RedirectDto redirect;

        /**
         * The app URI for the client.
         */
        private AppDto app;

        /**
         * Whether the client can display a user code.
         */
        @JsonProperty("user_code")
        private Boolean userCode;

        /**
         * The URI the client can send the user to for entering a user code.
         */
        @JsonProperty("user_code_uri")
        private String userCodeUri;

        /**
         * The hash method used for the interaction finish callback.
         */
        private String finish;
    }

    /**
     * DTO for redirect interaction mode.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RedirectDto {
        /**
         * The redirect URI for the client.
         */
        private String uri;

        /**
         * The nonce for the redirect.
         */
        private String nonce;
    }

    /**
     * DTO for app interaction mode.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AppDto {
        /**
         * The app URI for the client.
         */
        private String uri;

        /**
         * The nonce for the app.
         */
        private String nonce;
    }

    /**
     * DTO for key information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class KeyDto {
        /**
         * The proof method used by the key.
         */
        private String proof;

        /**
         * The JWK of the key.
         */
        private Map<String, Object> jwk;

        /**
         * The JWK set URI of the key.
         */
        @JsonProperty("jwks")
        private String jwksUri;

        /**
         * The certificate of the key.
         */
        private String cert;

        /**
         * The certificate URI of the key.
         */
        @JsonProperty("cert_S256")
        private String certS256;

        /**
         * The identifier of the key.
         */
        @JsonProperty("kid")
        private String keyId;
    }

    /**
     * DTO for display information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DisplayDto {
        /**
         * The name of the client.
         */
        private String name;

        /**
         * The URI of the client.
         */
        private String uri;

        /**
         * The logo URI of the client.
         */
        @JsonProperty("logo_uri")
        private String logoUri;
    }
}