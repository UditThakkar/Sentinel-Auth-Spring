package com.udit.authlib.entity;

import com.udit.authlib.entity.base.BaseIdEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseIdEntity {
  private String name;
}
