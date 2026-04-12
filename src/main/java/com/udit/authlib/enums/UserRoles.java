package com.udit.authlib.enums;

public enum UserRoles {
  ADMIN("admin"),
  USER("user");

  private final String displayName;

  UserRoles(String displayName) {
    this.displayName = displayName;
  }
}
