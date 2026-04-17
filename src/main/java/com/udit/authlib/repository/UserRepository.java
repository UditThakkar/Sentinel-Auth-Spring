package com.udit.authlib.repository;

import com.udit.authlib.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findUserByEmail(String email);

  Optional<User> findUserByUsername(String username);

  boolean existsByUsername(String username);

  boolean existsByEmail(@Email @NotBlank String email);
}
