package com.udit.authlib.entity;

import com.udit.authlib.entity.base.AuditableEntity;
import com.udit.authlib.enums.VerificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "verification_tokens")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerificationToken extends AuditableEntity {

  @Column(nullable = false, unique = true)
  private String token;

  @Column(nullable = false)
  private Instant expiry;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private VerificationType type;

  @ManyToOne
  @JoinColumn(name = "user_id", referencedColumnName = "id")
  private User user;
}
