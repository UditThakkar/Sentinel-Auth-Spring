package com.udit.authlib.security;

import com.udit.authlib.dto.JwtResponse;
import com.udit.authlib.dto.LoginRequest;
import com.udit.authlib.dto.SignupRequest;
import com.udit.authlib.entity.Role;
import com.udit.authlib.entity.User;
import com.udit.authlib.enums.UserStatus;
import com.udit.authlib.exception.UserAlreadyExistsException;
import com.udit.authlib.exception.UserLockedException;
import com.udit.authlib.repository.RoleRepository;
import com.udit.authlib.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final RoleRepository roleRepository;
  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwtUtils;


  @Transactional
  public void registerUser(SignupRequest request) {
    if(!validateRequest(request)) {
      throw new UserAlreadyExistsException("Username or email already exists");
    }
    var encodedPassword = passwordEncoder.encode(request.getPassword());
    Role roleToAssign = roleRepository.findByName("ROLE_USER");

    User userToSave = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(encodedPassword)
            .roles(Set.of(roleToAssign))
            .status(UserStatus.VERIFIED)
            .build();

    userRepository.save(userToSave);
  }

  @Transactional
  public JwtResponse authenticateUser(LoginRequest request) {
    try {
      Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
      SecurityContextHolder.getContext().setAuthentication(authentication);

      String token = jwtUtils.generateJwtToken(authentication);
      User user = (User) authentication.getPrincipal();

      assert user != null;
      user.setFailedLoginAttempts(0);
      user.setLockedUntil(null);
      userRepository.save(user);

      return JwtResponse.builder()
              .token(token)
              .username(user.getUsername())
              .roles(user.getRoles().stream().map(Role::getName).toList())
              .build();
    } catch (AuthenticationException e) {
      if (e instanceof LockedException) {
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
        if (user.getFailedLoginAttempts() >= 5) {
          Calendar cal = Calendar.getInstance();
          cal.add(Calendar.MINUTE, 60);
          user.setLockedUntil(cal.getTime());
          userRepository.save(user);
          throw new UserLockedException("Too many failed attempts, your account has been locked. Please try again after some time");
        } else {
          userRepository.save(user);
          throw new BadCredentialsException("Invalid username or password");
        }
      } else {
        throw new BadCredentialsException("Invalid username or password");
      }
    }
  }

  private boolean validateRequest(SignupRequest request) {
    return userRepository.findUserByUsername(request.getUsername()).isEmpty()
            && userRepository.findUserByEmail(request.getEmail()).isEmpty();
  }
}
