package com.udit.authlib.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
  VERIFIED("Verified"),
  UNVERIFIED("Unverified"),
  LOCKED("Locked");

  private final String displayName;

  UserStatus(String displayName) {
    this.displayName = displayName;
  }

}

