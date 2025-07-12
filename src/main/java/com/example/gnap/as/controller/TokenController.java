package com.example.gnap.as.controller;

import com.example.gnap.as.model.AccessToken;
import com.example.gnap.as.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for token management in the GNAP protocol.
 */
@RestController
@RequestMapping("/gnap")
public class TokenController {

    private static final Logger log = LoggerFactory.getLogger(TokenController.class);

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    /**
     * Introspect a token.
     *
     * @param token the token to introspect
     * @return the token introspection information
     */
    @PostMapping("/token/introspect")
    public ResponseEntity<AccessToken> introspectToken(@RequestParam String token) {
        log.info("Received token introspection request for token: {}", token);
        try {
            AccessToken accessToken = tokenService.introspectToken(token);
            return ResponseEntity.ok(accessToken);
        } catch (Exception e) {
            log.error("Error introspecting token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Revoke a token.
     *
     * @param token the token to revoke
     * @return a success response
     */
    @PostMapping("/token/revoke")
    public ResponseEntity<Void> revokeToken(@RequestParam String token) {
        log.info("Received token revocation request for token: {}", token);
        try {
            boolean revoked = tokenService.revokeToken(token);
            if (revoked) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error revoking token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
