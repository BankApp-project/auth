# 🏦 BankApp Authentication Service

> Modern, secure passwordless authentication microservice built with Spring Boot and Hexagonal Architecture

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/)
[![WebAuthn](https://img.shields.io/badge/WebAuthn-FIDO2-blue.svg)](https://webauthn.guide/)

## 🌐 Live Demo

**[Try it out →](https://auth.bankapp.online/)**

Experience passwordless authentication with WebAuthn/FIDO2 using your device's biometrics or hardware security keys.

> **Note:** Currently supports desktop (Windows, macOS, Linux) and Android. iOS compatibility under investigation.

---

## 📖 What is BankApp Auth?

A production-ready authentication microservice demonstrating modern security practices and clean architecture. Features **passwordless authentication** using WebAuthn/FIDO2, eliminating traditional password vulnerabilities.

**Key capabilities:**
- 📧 Email-based verification with secure OTP
- 🔐 Passwordless authentication via WebAuthn/FIDO2
- 📱 Multi-device support (biometrics, security keys)
- 🔔 Event-driven architecture with async messaging
- 🎫 Authorization token interface ready for JWT

### Authentication Flows

```mermaid
flowchart TD
    Start([User Starts]) --> Choice{Device known by system?}
    
    %% New User Registration Flow
    Choice -->|New User| IV[Initiate Verification]
    IV --> CV[Complete Verification]
    CV --> CR[Complete Registration]
    CR --> Success1[✅ Registered & Authenticated]
    
    %% Existing User Authentication Flow
    Choice -->|Existing User| IA[Initiate Authentication]
    IA --> CA[Complete Authentication]
    CA --> Success2[✅ Authenticated]
    
    %% Alternative Flow: Verification then Authentication
    CV -->|User Already Has Passkey| CA3[Complete Authentication]
    CA3 --> Success3[✅ Authenticated]
    
    %% Styling
    classDef useCase fill:#e1f5fe,stroke:#01579b,stroke-width:2px
    classDef success fill:#e8f5e8,stroke:#2e7d32,stroke-width:2px
    classDef decision fill:#fff3e0,stroke:#ef6c00,stroke-width:2px
    
    class IV,CV,IA,CA,CR,CA3 useCase
    class Success1,Success2,Success3 success
    class Choice decision
```

**Supported flows:**
1. **New User Registration**: Email Verification → WebAuthn Registration → Authorization Token Issuance
2. **Existing User Login**: WebAuthn Authentication → Authorization Token Issuance
3. **Alternative Flow**: Email Verification → WebAuthn Authentication

**Built with:**
- Hexagonal Architecture for clean separation and testability
- Domain-Driven Design principles
- Spring Boot 3.5 with Virtual Threads (Java 21+)
- PostgreSQL, Redis, RabbitMQ

---

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- Git

### Running Locally

```bash
# Clone and start
git clone <repository-url>
cd bankapp-auth
docker compose up -d

# Verify
curl http://localhost:8080/actuator/health
```

**Access points:**
- API Base: `http://localhost:8080/api`
- API Documentation: `http://localhost:8080/api` (Swagger UI)
- RabbitMQ Management: `http://localhost:15672`

> **Note:** The `/api` context path is configured via the `CONTEXT_PATH` environment variable in `.env`

> **Important:** This service publishes OTP events to RabbitMQ. For email delivery, you need a notification service consuming these events. See the [Notification Integration Guide](https://github.com/BankApp-project/auth/wiki/Notification-Integration).

---

## 📚 Documentation

- **[📖 Full Documentation](https://github.com/BankApp-project/auth/wiki)** - Complete technical documentation
- **[🔌 API Reference](https://auth.bankapp.online/api)** - Interactive Swagger UI
- **[⚙️ Configuration](https://github.com/BankApp-project/auth/wiki/Configuration)** - Environment setup and deployment
- **[📧 Notification Integration](https://github.com/BankApp-project/auth/wiki/Notification-Integration)** - External service setup

---

## ⚠️ Project Status

**Current:** Production-ready for demonstration purposes with live deployment

**Known limitations:**
- iOS/iPhone WebAuthn compatibility issue under investigation
- JWT token implementation pending (port-based design ready)
- Simplified WebAuthn setup (use strict mode for production)

See [Project Status & Limitations](https://github.com/BankApp-project/auth/wiki#%EF%B8%8F-project-status) in the wiki for production hardening checklist.

---

## 🛠️ Tech Stack

**Core:** Spring Boot 3.5, Java 21, Maven  
**Data:** PostgreSQL, Redis, Spring Data JPA, Flyway  
**Security:** Spring Security, WebAuthn4J  
**Messaging:** RabbitMQ, Spring AMQP  
**DevOps:** Docker, Docker Compose

---

## 🤝 Contributing

Contributions welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Follow existing architecture patterns
4. Add tests for new functionality
5. Submit a pull request

See the [Wiki](https://github.com/BankApp-project/auth/wiki) for development guidelines.

---

## 📄 License

MIT License - see LICENSE file for details.

---

## 🔗 Related Projects

- [Notification Service](https://github.com/BankApp-project/notification-service)
- [BankApp Frontend](https://github.com/BankApp-project/bankapp-auth-frontend)

---

**Questions?** Check the [Wiki](https://github.com/BankApp-project/auth/wiki) or open an issue.
