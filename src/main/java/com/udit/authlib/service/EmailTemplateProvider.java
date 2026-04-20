package com.udit.authlib.service;

import com.udit.authlib.dto.EmailModel;
import com.udit.authlib.entity.VerificationToken;

public interface EmailTemplateProvider {

  EmailModel buildVerificationEmail(VerificationToken token, String link);
  EmailModel buildPasswordResetEmail(VerificationToken token, String link);
}
