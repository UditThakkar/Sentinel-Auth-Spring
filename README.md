# Auth-Lib: Reusable Spring Boot Authentication Framework

A robust, API-only Java Spring Boot library designed to handle the core complexities of authentication and user management. This package is built for developers who want a drop-in security layer that is strictly stateless, highly configurable, and production-ready.

## 🛠 Roadmap & Feature Progress

Below is the master plan for the framework. Items marked with `[x]` are implemented and verified.

### 🔐 1. Core Authentication (The Foundation)
- [x] **Strictly Stateless Architecture**: JWT-based, no `HttpSession`.
- [x] **Custom Security Gateway**: Lambda-based `SecurityFilterChain` with custom `AuthenticationEntryPoint`.
- [x] **Unified Login**: Authentication via `username` OR `email`.
- [x] **Scalable Role Engine**: Many-to-Many JPA mapping for roles.
- [x] **JWT Utility Suite**: Generation, validation, and claim extraction.

### 🛡 2. Advanced Security (The Shield)
- [x] **Brute-Force Protection**: Automatic account locking after 5 failed attempts.
- [x] **Configurable Lockout**: Persistence-backed `lockedUntil` logic.
- [x] **Secure Hashing**: BCrypt password encoding.
- [x] **Global Error Management**: Structured JSON responses for all security exceptions.
- [ ] **Refresh Tokens**: Long-lived session management without credential re-entry (Ticket #8).
- [ ] **Two-Factor Authentication (2FA)**: TOTP support (Future).

### 👤 3. User & Account Management
- [x] **Flexible Registration**: Validation-backed signup API.
- [ ] **Email Verification**: Token-based onboarding flow (Ticket #9).
- [ ] **Password Reset**: Secure forgot-password flow via email tokens (Ticket #10).
- [ ] **Account Unlock**: Admin/Email-based unlock mechanism.
- [ ] **Profile Management**: API for users to update their own details.

### 📊 4. Compliance & Audit
- [ ] **Audit Logging**: Dedicated table to track every login, failure, and security change.
- [ ] **GDPR Suite**: Endpoints for data export and account "Right to be Forgotten".

---

## 📡 API Documentation

### 1. User Registration
**POST** `/api/auth/signup`
```json
{
  "username": "udit",
  "email": "udit@example.com",
  "password": "securePassword123",
  "firstName": "Udit",
  "lastName": "Sharma"
}
```

### 2. User Login
**POST** `/api/auth/signin`
```json
{
  "username": "udit@example.com", // Supports username OR email
  "password": "securePassword123"
}
```
**Response:**
```json
{
  "token": "eyJhbG...",
  "username": "udit",
  "roles": ["ROLE_USER"]
}
```

## 🔒 Security Features

### Account Lockout
The library automatically monitors failed login attempts. If a user fails to authenticate 5 times in a row, the `lockedUntil` timestamp is set, and the account is disabled for 60 minutes.
- **401 Unauthorized**: Returned for standard bad credentials.
- **423 Locked**: Returned once the threshold is reached.

### Global Exception Handling
All errors return a consistent JSON structure:
```json
{
  "errorCode": "USER_ALREADY_EXISTS",
  "message": "Username or email already exists",
  "timestamp": 1713370000000
}
```

## 🗺 Roadmap
- [ ] **Refresh Tokens**: Handle long-lived sessions without re-authentication.
- [ ] **Email Verification**: Asynchronous onboarding flow with verification links.
- [ ] **Password Reset**: Secure forgot-password flow with tokens.
- [ ] **Audit Logging**: Track all security-sensitive events in a dedicated table.

---
Developed as a high-performance, reusable backend component.
