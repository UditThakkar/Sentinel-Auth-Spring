package com.udit.authlib.entity;

import com.udit.authlib.entity.base.AuditableEntity;
import com.udit.authlib.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User extends AuditableEntity implements UserDetails {

  private String username;
  private String email;
  private String password;
  
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private UserStatus status = UserStatus.UNVERIFIED;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
          name = "user_roles",
          joinColumns = @JoinColumn(name = "user_id"),
          inverseJoinColumns = @JoinColumn(name = "role_id")
  )
  @Builder.Default
  private Set<Role> roles = new HashSet<>();

  private int failedLoginAttempts;

  private Date lockedUntil;

  private String firstName;
  private String lastName;

  @OneToOne(mappedBy = "user")
  private RefreshToken refreshToken;

  @OneToMany(mappedBy = "user")
  private List<VerificationToken> token;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
            .collect(Collectors.toList());
  }

  @Override
  public @Nullable String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return lockedUntil == null || lockedUntil.before(new Date());
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return status == UserStatus.VERIFIED;
  }
}
