package com.udit.authlib.repository;

import com.udit.authlib.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

  VerificationToken findByToken(String token);
}
