package com.udit.authlib.security;

import com.udit.authlib.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

    if(isEmail(username)) {
      return userRepository.findUserByEmail(username)
              .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    } else {
      return userRepository.findUserByUsername(username)
              .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
  }

  /**
   * Simple email validation method
   * Checks if the input string contains '@' symbol
   * @param input the string to validate
   * @return true if input appears to be an email, false otherwise
   */
  private boolean isEmail(String input) {
    return input != null && input.contains("@");
  }
}
