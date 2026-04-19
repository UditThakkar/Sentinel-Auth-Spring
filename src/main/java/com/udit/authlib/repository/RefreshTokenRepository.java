package com.udit.authlib.repository;

import com.udit.authlib.entity.RefreshToken;
import com.udit.authlib.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  RefreshToken findByToken(String token);

  void deleteByUser(User user);
}
