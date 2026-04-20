# AuthLib: Reusable Spring Boot Authentication Starter

A professional, "Spring Boot Starter" library designed to handle the core complexities of authentication, user management, and security auditing. Drop this into any Spring Boot project to instantly gain a full-featured security layer.

## 🚀 Features

- **JWT Authentication**: Secure, stateless sessions with automated token generation and validation.
- **Refresh Token System**: Long-lived sessions without sacrificing security.
- **Account Security**: Automatic account locking after 5 failed attempts (60-minute lockout).
- **Email Verification**: Token-based onboarding flow with customizable email links.
- **Password Recovery**: Secure forgot-password flow via email tokens.
- **Audit Logging**: Asynchronous logging of all security events (Login, Signup, Reset, Lockout) with IP tracking.
- **Customizable Emailing**: Pluggable `EmailTemplateProvider` and `EmailService`.
- **Auto-Configuration**: Zero-boilerplate setup—just add the dependency and properties.

---

## 📦 Installation

### 1. Build the Library
Run the following command in the library root:
```bash
./gradlew publishToMavenLocal
```

### 2. Add Dependency
In your **host application's** `build.gradle`:
```gradle
dependencies {
    implementation 'com.udit:auth-lib:0.0.1-SNAPSHOT'
}
```

---

## ⚙️ Configuration

Add the following to your `application.properties`:

### Required System Properties
```properties
# DataSource (Required for JPA)
spring.datasource.url=jdbc:postgresql://localhost:5432/yourdb
spring.datasource.username=postgres
spring.datasource.password=password

# Mail Setup (Required for Email Service)
spring.mail.host=localhost
spring.mail.port=1025
spring.mail.username=support@myapp.com
spring.mail.password=password
```

### AuthLib Custom Properties
| Property | Default | Description |
|----------|---------|-------------|
| `auth-api.jwtSecret` | (dev-only-secret) | Secret key for JWT signing |
| `auth-api.jwtExpirationMs` | `3600000` (1h) | JWT access token validity |
| `auth-api.baseEndpoint` | `/api/auth` | Base path for all auth APIs |
| `auth-api.frontendUrl` | `http://localhost:8080` | URL for email links |
| `auth-api.appName` | `AuthLib` | App name used in email templates |
| `auth-api.emailFromName` | `AuthLib Support` | Display name for sender |
| `auth-api.tokenParamName` | `token` | Query param name in email links |

---

## 🎨 Customization

### Overriding Email Templates
To use your own HTML templates (e.g., Thymeleaf), implement the `EmailTemplateProvider` interface and mark it as a `@Bean` or `@Component` in your project:

```java
@Component
@Primary
public class MyCustomTemplateProvider implements EmailTemplateProvider {
    @Override
    public EmailModel buildVerificationEmail(VerificationToken token, String link) {
        return EmailModel.builder()
            .subject("Welcome!")
            .body("<h1>Hello!</h1> <a href='" + link + "'>Click here</a>")
            .build();
    }
    // ... implement other methods
}
```

### Overriding the Email Service
If you want to use AWS SES or SendGrid instead of SMTP, implement the `EmailService` interface:

```java
@Component
@Primary
public class SendGridEmailService implements EmailService {
    // Your implementation here
}
```

---

## 🔍 Audit Logging
The library automatically tracks security events in the `audit_logs` table.
- **Events Tracked**: `LOGIN_SUCCESS`, `LOGIN_FAILURE`, `SIGNUP_SUCCESS`, `ACCOUNT_LOCKED`, `PASSWORD_RESET_REQUESTED`, `PASSWORD_RESET_SUCCESS`.
- **IP Tracking**: All logs include the source IP address of the request.

To view logs, you can inject the `AuditLogRepository` into your own admin controllers.

---
Developed by **Udit** as a high-performance, reusable security component.
