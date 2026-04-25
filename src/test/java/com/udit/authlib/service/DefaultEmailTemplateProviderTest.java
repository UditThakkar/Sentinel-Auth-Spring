package com.udit.authlib.service;

import com.udit.authlib.dto.EmailModel;
import com.udit.authlib.entity.VerificationToken;
import com.udit.authlib.enums.VerificationType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultEmailTemplateProviderTest {

    private final DefaultEmailTemplateProvider templateProvider = new DefaultEmailTemplateProvider();

    @Test
    void buildVerificationEmailIncludesSubjectAndLink() {
        VerificationToken token = VerificationToken.builder()
                .token("verification-token")
                .type(VerificationType.EMAIL_VERIFICATION)
                .build();

        EmailModel email = templateProvider.buildVerificationEmail(token, "https://example.com/verify?token=verification-token");

        assertThat(email.getSubject()).isEqualTo("Verify Your Email Address");
        assertThat(email.getBody()).contains("Verify Email", "https://example.com/verify?token=verification-token");
    }

    @Test
    void buildPasswordResetEmailIncludesSubjectAndLink() {
        VerificationToken token = VerificationToken.builder()
                .token("reset-token")
                .type(VerificationType.PASSWORD_RESET)
                .build();

        EmailModel email = templateProvider.buildPasswordResetEmail(token, "https://example.com/reset-password?token=reset-token");

        assertThat(email.getSubject()).isEqualTo("Password Reset Request");
        assertThat(email.getBody()).contains("Reset Password", "https://example.com/reset-password?token=reset-token");
    }
}
