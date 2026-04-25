package com.udit.authlib.entity;

import com.udit.authlib.entity.base.BaseIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "token_blacklist")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistedToken extends BaseIdEntity {

    @Column(nullable = false, unique = true, length = 1000)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;
}
