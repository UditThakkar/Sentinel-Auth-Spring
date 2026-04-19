package com.udit.authlib.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Data
@RequiredArgsConstructor
public class RefreshToken {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "user_id", referencedColumnName = "id")
  private User user;

  private String token;

  private Instant expiryDate;
}
