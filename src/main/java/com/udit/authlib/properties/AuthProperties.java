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
     * Refresh token expiration time in milliseconds.
     */
    private long refreshTokenExpirationMs = 604800000; // 7 days

    private String baseEndpoint = "/api/auth";

    /**
     * Relative endpoint for the sign-in API (appended to baseEndpoint).
     */
    private String signinEndpoint = "/signin";

    /**
     * Relative endpoint for the sign-up API (appended to baseEndpoint).
     */
    private String signupEndpoint = "/signup";

    /**
     * Relative endpoint for the refresh API (appended to baseEndpoint).
     */
    private String refreshEndpoint = "/refresh";
}
