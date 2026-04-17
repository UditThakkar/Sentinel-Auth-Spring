# Auth-Lib: Reusable Spring Boot Authentication Framework

A robust, API-only Java Spring Boot library designed to handle the core complexities of authentication and user management. This package is built for developers who want a drop-in security layer that is strictly stateless, highly configurable, and production-ready.

## 🚀 Features

- **Strictly Stateless Authentication**: Pure JWT-based flow with no session or cookie dependencies.
- **Unified Login**: Support for logging in via either `username` or `email` out of the box.
- **Secure Registration**: Managed signup flow with password hashing (BCrypt) and automatic role assignment.
- **Account Security (Brute-Force Protection)**:
  - Automatic account locking after 5 failed attempts.
  - Configurable lockout duration (default: 60 minutes).
  - Persistence-backed tracking of failed attempts.
- **Scalable Role Management**: Table-based Many-to-Many role system (transitioned from Enums for better scalability).
- **Professional Error Handling**: Global `@RestControllerAdvice` providing structured JSON error responses.
- **Configurable Endpoints**: All authentication endpoints are fully configurable via Spring Properties.

## 🛠 Technologies

- **Java 17+**
- **Spring Boot 3.x**
- **Spring Security 6.x**
- **Spring Data JPA**
- **JSON Web Tokens (jjwt)**
- **PostgreSQL**
- **Lombok**

## ⚙️ Configuration

Add the following properties to your `application.properties` to customize the library:

```properties
# JWT Configuration
auth-api.jwtSecret=your-very-long-and-secure-secret-key
auth-api.jwtExpirationMs=3600000

# Endpoint Configuration
auth-api.baseEndpoint=/api/auth
auth-api.signinEndpoint=/signin
auth-api.signupEndpoint=/signup
```

## 📂 Project Structure

```text
com.udit.authlib
├── controller      # REST Controllers (AuthController)
├── dto             # Data Transfer Objects (Requests/Responses)
├── entity          # JPA Entities (User, Role)
├── enums           # Shared Enums (UserStatus)
├── exception       # Custom Exceptions & Global Handler
├── properties      # Library Configuration (AuthProperties)
├── repository      # Data Access Layers (UserRepository, RoleRepository)
└── security        # Core Logic (JwtUtils, AuthService, SecurityConfig)
```

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
