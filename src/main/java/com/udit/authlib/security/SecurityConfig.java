package com.udit.authlib.security;

import com.udit.authlib.properties.AuthProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Main security configuration for the authentication library.
 * Configures the filter chain, stateless sessions, and authentication infrastructure.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final UserDetailsService userDetailsService;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final AuthProperties authProperties;

  /**
   * Defines the security filter chain, setting up stateless session management, 
   * CSRF protection, and authorization rules for endpoints.
   *
   * @param http the HttpSecurity to configure
   * @return the configured SecurityFilterChain
   * @throws Exception if an error occurs during configuration
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    String base = authProperties.getBaseEndpoint();
    
    http
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers(base + authProperties.getSigninEndpoint()).permitAll()
                    .requestMatchers(base + authProperties.getSignupEndpoint()).permitAll()
                    .requestMatchers(base + authProperties.getVerifyEndpoint()).permitAll()
                    .requestMatchers(base + authProperties.getForgotPasswordEndpoint()).permitAll()
                    .requestMatchers(base + authProperties.getResetPasswordEndpoint()).permitAll()
                    .requestMatchers(base + authProperties.getRefreshEndpoint()).permitAll()
                    .anyRequest().authenticated()
            )

            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  /**
   * Custom entry point to handle unauthorized requests and return a 401 JSON error.
   *
   * @return the configured AuthenticationEntryPoint
   */
  @Bean
  public AuthenticationEntryPoint unauthorizedHandler() {
    return (request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
  }

  /**
   * Provides the BCrypt password encoder for secure hashing of user passwords.
   *
   * @return the configured PasswordEncoder
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Exposes the AuthenticationManager bean for use in the authentication process.
   *
   * @param authConfig the AuthenticationConfiguration to retrieve the manager from
   * @return the AuthenticationManager
   * @throws Exception if an error occurs while retrieving the manager
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }
}
