package com.udit.authlib.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

import com.udit.authlib.repository.AuditLogRepository;
import com.udit.authlib.repository.RefreshTokenRepository;
import com.udit.authlib.repository.RoleRepository;
import com.udit.authlib.repository.UserRepository;
import com.udit.authlib.repository.VerificationTokenRepository;
import com.udit.authlib.security.*;
import com.udit.authlib.service.*;
import com.udit.authlib.properties.AuthProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@AutoConfiguration

@EnableJpaRepositories(basePackages = "com.udit.authlib.repository")
@EntityScan(basePackages = "com.udit.authlib.entity")
@ConfigurationPropertiesScan(basePackages = "com.udit.authlib.properties")
@EnableAsync
@Import({SecurityConfig.class, JwtUtils.class, JwtAuthenticationFilter.class})
@ComponentScan(basePackages = {
        "com.udit.authlib.controller",
        "com.udit.authlib.exception"
})
public class AuthLibAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EmailTemplateProvider emailTemplateProvider() {
        return new DefaultEmailTemplateProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public EmailService emailService(JavaMailSender mailSender, AuthProperties authProperties, EmailTemplateProvider templateProvider) {
        return new DefaultSmtpEmailService(mailSender, authProperties, templateProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public VerificationTokenService verificationTokenService(VerificationTokenRepository tokenRepository, AuthProperties authProperties, UserRepository userRepository) {
        return new VerificationTokenService(tokenRepository, authProperties, userRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditService auditService(AuditLogRepository auditLogRepository) {
        return new AuditService(auditLogRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public RefreshTokenService refreshTokenService(RefreshTokenRepository refreshTokenRepository, AuthProperties authProperties) {
        return new RefreshTokenService(refreshTokenRepository, authProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthService authService(UserRepository userRepository, 
                                 PasswordEncoder passwordEncoder, 
                                 RoleRepository roleRepository, 
                                 AuthenticationManager authenticationManager, 
                                 JwtUtils jwtUtils, 
                                 RefreshTokenService refreshTokenService, 
                                 VerificationTokenService verificationTokenService, 
                                 EmailService emailService, 
                                 AuditService auditService) {
        return new AuthService(userRepository, passwordEncoder, roleRepository, authenticationManager, jwtUtils, refreshTokenService, verificationTokenService, emailService, auditService);
    }
}
