package com.udit.authlib.service;

import com.udit.authlib.entity.VerificationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

  public void sendVerificationEmail(VerificationToken token) {
    String link = "http://localhost:8080/api/auth/verify?token=" + token.getToken();
    log.info("Sending verification email with link {}", link);
  }
}
