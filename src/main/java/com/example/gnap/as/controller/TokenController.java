package com.example.gnap.as.controller;

import com.example.gnap.as.dto.TokenIntrospectionDto;
import com.example.gnap.as.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for token management in the GNAP protocol.
 */
@RestController
@RequestMapping("/gnap")
@RequiredArgsConstructor
@Slf4j
public class TokenController {

    private final TokenService tokenService;

    /**
     * Introspect a token.
     *
     * @param token the token to introspect
     * @return the token introspection DTO
     */
    @PostMapping("/token/introspect")
    public ResponseEntity<TokenIntrospectionDto> introspectToken(@RequestParam String token) {
        log.info("Received token introspection request for token: {}", token);
        try {
            TokenIntrospectionDto introspectionDto = tokenService.introspectToken(token);
            return ResponseEntity.ok(introspectionDto);
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