package com.udit.authlib.controller;

import com.udit.authlib.dto.ForgotPasswordRequest;
import com.udit.authlib.dto.JwtResponse;
import com.udit.authlib.dto.LoginRequest;
import com.udit.authlib.dto.ResetPasswordRequest;
import com.udit.authlib.dto.SignupRequest;
import com.udit.authlib.dto.TokenRefreshRequest;
import com.udit.authlib.dto.TokenRefreshResponse;
import com.udit.authlib.entity.User;
import com.udit.authlib.security.AuthService;
import com.udit.authlib.service.VerificationTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private static final String IP_ADDRESS = "127.0.0.1";

    @Mock
    private AuthService authService;
    @Mock
    private VerificationTokenService verificationTokenService;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthController authController;

    @Test
    void authenticateUserDelegatesToServiceAndReturnsResponse() {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password")
                .build();
        JwtResponse jwtResponse = JwtResponse.builder()
                .token("jwt-token")
                .refreshToken("refresh-token")
                .username("testuser")
                .roles(List.of("ROLE_USER"))
                .build();
        when(request.getRemoteAddr()).thenReturn(IP_ADDRESS);
        when(authService.authenticateUser(loginRequest, IP_ADDRESS)).thenReturn(jwtResponse);

        ResponseEntity<?> response = authController.authenticateUser(loginRequest, request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isSameAs(jwtResponse);
    }

    @Test
    void registerUserDelegatesToServiceAndReturnsSuccessMessage() {
        SignupRequest signupRequest = SignupRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();
        when(request.getRemoteAddr()).thenReturn(IP_ADDRESS);

        ResponseEntity<?> response = authController.registerUser(signupRequest, request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("User registered successfully!");
        verify(authService).registerUser(signupRequest, IP_ADDRESS);
    }

    @Test
    void refreshTokenDelegatesToServiceAndReturnsResponse() {
        TokenRefreshRequest refreshRequest = TokenRefreshRequest.builder()
                .refreshToken("refresh-token")
                .build();
        TokenRefreshResponse refreshResponse = TokenRefreshResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("refresh-token")
                .build();
        when(authService.refreshToken(refreshRequest)).thenReturn(refreshResponse);

        ResponseEntity<?> response = authController.refreshToken(refreshRequest);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isSameAs(refreshResponse);
    }

    @Test
    void verifyTokenDelegatesToVerificationServiceAndReturnsSuccessMessage() {
        ResponseEntity<?> response = authController.verifyToken("verification-token");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Token verified successfully");
        verify(verificationTokenService).verifyToken("verification-token");
    }

    @Test
    void forgotPasswordDelegatesToServiceAndReturnsEnumerationSafeMessage() {
        ForgotPasswordRequest forgotPasswordRequest = new ForgotPasswordRequest("test@example.com");
        when(request.getRemoteAddr()).thenReturn(IP_ADDRESS);

        ResponseEntity<?> response = authController.forgotPassword(forgotPasswordRequest, request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("If the email exists in our system, you will receive a password reset link shortly");
        verify(authService).requestPasswordReset("test@example.com", IP_ADDRESS);
    }

    @Test
    void resetPasswordDelegatesToServiceAndReturnsSuccessMessage() {
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest("reset-token", "new-password");
        when(request.getRemoteAddr()).thenReturn(IP_ADDRESS);

        ResponseEntity<?> response = authController.resetPassword(resetPasswordRequest, request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Password has been reset successfully. You can now log in with your new password.");
        verify(authService).resetPassword("reset-token", "new-password", IP_ADDRESS);
    }

    @Test
    void logoutExtractsBearerTokenAndDelegatesToService() {
        User user = User.builder()
                .username("testuser")
                .build();
        when(request.getHeader("Authorization")).thenReturn("Bearer jwt-token");
        when(request.getRemoteAddr()).thenReturn(IP_ADDRESS);

        ResponseEntity<?> response = authController.logout(user, request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("User logged out successfully");
        verify(authService).logout("jwt-token", user, IP_ADDRESS);
    }
}
