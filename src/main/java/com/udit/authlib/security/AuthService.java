package com.udit.authlib.security;

import com.udit.authlib.dto.JwtResponse;
import com.udit.authlib.dto.LoginRequest;
import com.udit.authlib.dto.SignupRequest;
import com.udit.authlib.dto.TokenRefreshRequest;
import com.udit.authlib.dto.TokenRefreshResponse;
import com.udit.authlib.entity.Role;
import com.udit.authlib.entity.User;
import com.udit.authlib.entity.RefreshToken;
import com.udit.authlib.entity.VerificationToken;
import com.udit.authlib.enums.UserStatus;
import com.udit.authlib.exception.UserAlreadyExistsException;
import com.udit.authlib.exception.UserLockedException;
import com.udit.authlib.repository.RoleRepository;
import com.udit.authlib.repository.UserRepository;
import com.udit.authlib.service.EmailService;
import com.udit.authlib.service.VerificationTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final RoleRepository roleRepository;
  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwtUtils;
  private final RefreshTokenService refreshTokenService;
  private final VerificationTokenService verificationTokenService;
  private final EmailService emailService;


  @Transactional
  public void registerUser(SignupRequest request) {
    log.info("Starting user registration for username: {}", request.getUsername());
    if(!validateRequest(request)) {
      log.warn("Registration failed - Username or email already exists: {}", request.getUsername());
      throw new UserAlreadyExistsException("Username or email already exists");
    }
    var encodedPassword = passwordEncoder.encode(request.getPassword());
    Role roleToAssign = roleRepository.findByName("ROLE_USER");

    User userToSave = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(encodedPassword)
            .roles(Set.of(roleToAssign))
            .status(UserStatus.UNVERIFIED)
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .build();

    userRepository.save(userToSave);

    VerificationToken verificationToken = verificationTokenService.generateVerificationToken(userToSave);
    emailService.sendVerificationEmail(verificationToken);

    log.info("User registered successfully - Username: {}, Email: {}", request.getUsername(), request.getEmail());
  }

  @Transactional
  public JwtResponse authenticateUser(LoginRequest request) {
    log.info("Authentication attempt for user: {}", request.getUsername());
    try {
      Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
      SecurityContextHolder.getContext().setAuthentication(authentication);

      String token = jwtUtils.generateJwtToken(authentication);
      User user = (User) authentication.getPrincipal();

      assert user != null;
      user.setFailedLoginAttempts(0);
      user.setLockedUntil(null);
      userRepository.save(user);
      log.debug("Failed login attempts reset for user: {}", user.getUsername());

      String refreshToken = refreshTokenService.generateRefreshToken(user).getToken();

      log.info("User authenticated successfully - Username: {}, Roles: {}", user.getUsername(), user.getRoles().stream().map(Role::getName).toList());
      return JwtResponse.builder()
              .token(token)
              .refreshToken(refreshToken)
              .username(user.getUsername())
              .roles(user.getRoles().stream().map(Role::getName).toList())
              .build();
    } catch (DisabledException e) {
      log.warn("Account disabled exception for user: {} - User account status is not verified", request.getUsername());
      throw e;
    } catch (AuthenticationException e) {
      if (e instanceof LockedException) {
        log.warn("Account locked exception for user: {}", request.getUsername());
        throw new UserLockedException("Account is locked due to too many failed attempts");
      }

      String identifier = request.getUsername();
      Optional<User> userOpt;
      if (identifier.contains("@")) {
        userOpt = userRepository.findUserByEmail(identifier);
      } else {
        userOpt = userRepository.findUserByUsername(identifier);
      }

      if (userOpt.isPresent()) {
        User user = userOpt.get();
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        log.warn("Failed login attempt #{} for user: {}", user.getFailedLoginAttempts(), identifier);
        if (user.getFailedLoginAttempts() >= 5) {
          Calendar cal = Calendar.getInstance();
          cal.add(Calendar.MINUTE, 60);
          user.setLockedUntil(cal.getTime());
          userRepository.save(user);
          log.error("Account locked due to 5 failed attempts - User: {}", identifier);
          throw new UserLockedException("Too many failed attempts, your account has been locked. Please try again after some time");
        } else {
          userRepository.save(user);
          log.debug("Bad credentials for user: {}", identifier);
          throw new BadCredentialsException("Invalid username or password");
        }
      } else {
        log.debug("User not found for identifier: {}", identifier);
        throw new BadCredentialsException("Invalid username or password");
      }
    }
  }

  @Transactional
  public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
    log.info("Token refresh request received");
    RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());
    User user = refreshToken.getUser();
    String newAccessToken = jwtUtils.generateJwtToken(new UsernamePasswordAuthenticationToken(user.getUsername(), null, user.getAuthorities()));
    log.info("Token refreshed successfully for user: {}", user.getUsername());
    return TokenRefreshResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(request.getRefreshToken())
            .build();
  }

  private boolean validateRequest(SignupRequest request) {
    return userRepository.findUserByUsername(request.getUsername()).isEmpty()
            && userRepository.findUserByEmail(request.getEmail()).isEmpty();
  }
}
