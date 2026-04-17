package com.udit.authlib.security;

import com.udit.authlib.dto.JwtResponse;
import com.udit.authlib.dto.LoginRequest;
import com.udit.authlib.dto.SignupRequest;
import com.udit.authlib.entity.Role;
import com.udit.authlib.entity.User;
import com.udit.authlib.enums.UserStatus;
import com.udit.authlib.exception.UserAlreadyExistsException;
import com.udit.authlib.repository.RoleRepository;
import com.udit.authlib.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final RoleRepository roleRepository;
  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwtUtils;


  public void registerUser(SignupRequest request) {
    if(validateRequest(request)) {
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

  public JwtResponse authenticateUser(LoginRequest request) {
    Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    String token = jwtUtils.generateJwtToken(authentication);
    User user = (User) authentication.getPrincipal();

    return JwtResponse.builder()
            .token(token)
            .username(user.getUsername())
            .roles(List.copyOf(user.getRoles()))
            .build();
  }

  private boolean validateRequest(SignupRequest request) {
    return userRepository.findUserByUsername(request.getUsername()).isEmpty()
            && userRepository.findUserByEmail(request.getEmail()).isEmpty();
  }
}
