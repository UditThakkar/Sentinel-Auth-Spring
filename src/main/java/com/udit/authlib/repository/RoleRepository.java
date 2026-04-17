package com.udit.authlib.repository;

import com.udit.authlib.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

  public Role findByName(String name);
}
