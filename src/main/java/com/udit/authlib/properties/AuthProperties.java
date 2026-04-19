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

    private long verificationTokenExpiry = 3600000; // 1 hour

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

    /**
     * Relative endpoint for the verification API (appended to baseEndpoint).
     */
    private String verifyEndpoint = "/verify";

    /**
     * Relative endpoint for the forgot password API (appended to baseEndpoint).
     */
    private String forgotPasswordEndpoint = "/forgot-password";

    /**
     * Relative endpoint for the reset password API (appended to baseEndpoint).
     */
    private String resetPasswordEndpoint = "/reset-password";
}
