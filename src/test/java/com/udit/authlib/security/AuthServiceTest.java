package com.udit.authlib.security;

import com.udit.authlib.dto.JwtResponse;
import com.udit.authlib.dto.LoginRequest;
import com.udit.authlib.dto.SignupRequest;
import com.udit.authlib.dto.TokenRefreshRequest;
import com.udit.authlib.dto.TokenRefreshResponse;
import com.udit.authlib.entity.RefreshToken;
import com.udit.authlib.entity.Role;
import com.udit.authlib.entity.User;
import com.udit.authlib.entity.VerificationToken;
import com.udit.authlib.enums.UserStatus;
import com.udit.authlib.enums.VerificationType;
import com.udit.authlib.exception.UserAlreadyExistsException;
import com.udit.authlib.exception.UserLockedException;
import com.udit.authlib.repository.RoleRepository;
import com.udit.authlib.repository.UserRepository;
import com.udit.authlib.service.AuditService;
import com.udit.authlib.service.EmailService;
import com.udit.authlib.service.TokenBlacklistService;
import com.udit.authlib.service.VerificationTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String IP_ADDRESS = "127.0.0.1";

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private VerificationTokenService verificationTokenService;
    @Mock
    private EmailService emailService;
    @Mock
    private AuditService auditService;
    @Mock
    private TokenBlacklistService blacklistService;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private SignupRequest signupRequest;

    @BeforeEach
    void setUp() {
        signupRequest = SignupRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();
    }

    @Test
    void registerUserSavesUnverifiedUserAndSendsVerificationEmail() {
        Role role = new Role("ROLE_USER");
        VerificationToken token = VerificationToken.builder()
                .token("verification-token")
                .type(VerificationType.EMAIL_VERIFICATION)
                .build();

        when(userRepository.findUserByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findUserByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(role);
        when(verificationTokenService.generateVerificationToken(any(User.class))).thenReturn(token);

        authService.registerUser(signupRequest, IP_ADDRESS);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.UNVERIFIED);
        assertThat(savedUser.getRoles()).containsExactly(role);
        assertThat(savedUser.getFirstName()).isEqualTo("Test");
        assertThat(savedUser.getLastName()).isEqualTo("User");

        verify(emailService).sendVerificationEmail(token);
        verify(auditService).log("testuser", "SIGNUP_SUCCESS", IP_ADDRESS, "User registered successfully");
    }

    @Test
    void registerUserThrowsWhenUsernameAlreadyExists() {
        when(userRepository.findUserByUsername("testuser")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.registerUser(signupRequest, IP_ADDRESS))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Username or email already exists");

        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendVerificationEmail(any());
        verify(auditService).log("testuser", "SIGNUP_FAILURE", IP_ADDRESS, "Username or email already exists");
    }

    @Test
    void authenticateUserReturnsJwtResponseAndResetsFailedAttempts() {
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();
        User user = User.builder()
                .username("testuser")
                .roles(Set.of(new Role("ROLE_USER")))
                .failedLoginAttempts(3)
                .build();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token("refresh-token")
                .expiryDate(Instant.now().plusSeconds(60))
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("jwt-token");
        when(refreshTokenService.generateRefreshToken(user)).thenReturn(refreshToken);

        JwtResponse response = authService.authenticateUser(request, IP_ADDRESS);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getRoles()).containsExactly("ROLE_USER");
        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLockedUntil()).isNull();
        verify(userRepository).save(user);
        verify(auditService).log("testuser", "LOGIN_SUCCESS", IP_ADDRESS, "User authenticated successfully");
    }

    @Test
    void authenticateUserIncrementsFailedAttemptsForExistingUser() {
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("wrong")
                .build();
        User user = User.builder()
                .username("testuser")
                .failedLoginAttempts(1)
                .build();

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));
        when(userRepository.findUserByUsername("testuser")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.authenticateUser(request, IP_ADDRESS))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid username or password");

        assertThat(user.getFailedLoginAttempts()).isEqualTo(2);
        assertThat(user.getLockedUntil()).isNull();
        verify(userRepository).save(user);
        verify(auditService).log("testuser", "LOGIN_FAILURE", IP_ADDRESS, "Invalid credentials - Attempt #2");
    }

    @Test
    void authenticateUserLocksAccountOnFifthFailedAttempt() {
        LoginRequest request = LoginRequest.builder()
                .username("test@example.com")
                .password("wrong")
                .build();
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .failedLoginAttempts(4)
                .build();

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));
        when(userRepository.findUserByEmail("test@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.authenticateUser(request, IP_ADDRESS))
                .isInstanceOf(UserLockedException.class)
                .hasMessage("Too many failed attempts, your account has been locked. Please try again after some time");

        assertThat(user.getFailedLoginAttempts()).isEqualTo(5);
        assertThat(user.getLockedUntil()).isNotNull();
        verify(userRepository).save(user);
        verify(auditService).log("test@example.com", "ACCOUNT_LOCKED", IP_ADDRESS, "Account locked due to 5 failed attempts");
    }

    @Test
    void refreshTokenValidatesRefreshTokenAndReturnsNewAccessToken() {
        User user = User.builder()
                .username("testuser")
                .roles(Set.of(new Role("ROLE_USER")))
                .build();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token("refresh-token")
                .expiryDate(Instant.now().plusSeconds(60))
                .build();
        TokenRefreshRequest request = TokenRefreshRequest.builder()
                .refreshToken("refresh-token")
                .build();

        when(refreshTokenService.validateRefreshToken("refresh-token")).thenReturn(refreshToken);
        when(jwtUtils.generateJwtToken(any())).thenReturn("new-access-token");

        TokenRefreshResponse response = authService.refreshToken(request);

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void requestPasswordResetSendsResetEmailForExistingUser() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .build();
        VerificationToken token = VerificationToken.builder()
                .user(user)
                .token("reset-token")
                .type(VerificationType.PASSWORD_RESET)
                .build();
        when(userRepository.findUserByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(verificationTokenService.generateVerificationToken(user, VerificationType.PASSWORD_RESET)).thenReturn(token);

        authService.requestPasswordReset("test@example.com", IP_ADDRESS);

        verify(emailService).sendPasswordResetEmail(token);
        verify(auditService).log("testuser", "PASSWORD_RESET_REQUESTED", IP_ADDRESS, "Password reset link sent to email");
    }

    @Test
    void requestPasswordResetLogsButDoesNotSendEmailForUnknownAddress() {
        when(userRepository.findUserByEmail("missing@example.com")).thenReturn(Optional.empty());

        authService.requestPasswordReset("missing@example.com", IP_ADDRESS);

        verify(verificationTokenService, never()).generateVerificationToken(any(), eq(VerificationType.PASSWORD_RESET));
        verify(emailService, never()).sendPasswordResetEmail(any());
        verify(auditService).log("missing@example.com", "PASSWORD_RESET_REQUESTED", IP_ADDRESS, "Non-existent email requested password reset");
    }

    @Test
    void resetPasswordUpdatesPasswordAndDeletesToken() {
        User user = User.builder()
                .username("testuser")
                .password("old-password")
                .build();
        when(verificationTokenService.validateTokenAndGetUser("reset-token", VerificationType.PASSWORD_RESET)).thenReturn(user);
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-password");

        authService.resetPassword("reset-token", "new-password", IP_ADDRESS);

        assertThat(user.getPassword()).isEqualTo("encoded-new-password");
        verify(userRepository).save(user);
        verify(verificationTokenService).deleteToken("reset-token");
        verify(auditService).log("testuser", "PASSWORD_RESET_SUCCESS", IP_ADDRESS, "Password reset successful");
    }

    @Test
    void logoutDeletesRefreshTokensAndBlacklistsJwt() {
        User user = User.builder()
                .username("testuser")
                .build();

        authService.logout("jwt-token", user, IP_ADDRESS);

        verify(refreshTokenService).deleteByUser(user);
        verify(blacklistService).blacklistToken("jwt-token");
        verify(auditService).log("testuser", "LOGOUT", IP_ADDRESS, "User logged out and token blacklisted");
    }
}
