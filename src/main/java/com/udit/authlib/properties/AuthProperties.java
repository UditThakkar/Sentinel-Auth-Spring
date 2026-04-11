package com.udit.authlib.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "auth-api")
public class AuthProperties {

    /**
     * Secret key for JWT signing. Should be a long, complex string.
     */
    private String jwtSecret = "defaultSecretKeyForDevelopmentOnlyPleaseChangeInProduction";

    /**
     * JWT expiration time in milliseconds.
     */
    private long jwtExpirationMs = 3600000; // 1 hour

    /**
     * Custom endpoint for the sign-in API.
     */
    private String signinEndpoint = "/api/auth/signin";
}
