# GNAP Authorization Server

A Java implementation of the Grant Negotiation and Authorization Protocol (GNAP) Authorization Server.

## Overview

This project implements an Authorization Server based on the [Grant Negotiation and Authorization Protocol (GNAP)](https://datatracker.ietf.org/doc/html/draft-ietf-gnap-core-protocol) specification, which is designed as a modern replacement for OAuth 2.0 and OpenID Connect.

GNAP provides a flexible framework for delegated authorization, allowing clients to request access to resources from an authorization server with enhanced security features and more flexible interaction modes compared to OAuth 2.0.

## Features

- Complete GNAP protocol implementation
- Multiple interaction modes (redirect, app, user code)
- Token management (issuance, introspection, revocation)
- Grant request processing and continuation
- User consent flows
- H2 in-memory database for development
- Liquibase for database migrations

## Technologies

- Java 24
- Spring Boot 3.5.3
- Spring Data JPA
- Spring Security
- H2 Database (for development)
- Liquibase
- JSON Web Tokens (JWT)

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Installation

1. Clone the repository:
   ```bash
   git clonehttps://github.com/isaacpeel/solesonic-gnap-as.git
   cd solesonic-gnap-as
   ```

2. Build the project:
   ```bash
   ./mvnw clean install
   ```

3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

The server will start on port 8080 by default.

## Configuration

The application can be configured through the `application.properties` file:

```properties
# Server configuration
server.port=8080

# Database configuration
spring.datasource.url=jdbc:h2:mem:gnapdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password

# GNAP AS configuration
gnap.as.issuer=https://auth.example.com
gnap.as.token.lifetime=3600
gnap.as.interaction.timeout=300
```

## API Endpoints

### Grant Management

- `POST /gnap/grant` - Process a new grant request
- `GET /gnap/grant/{grantId}` - Process a continuation request
- `PUT /gnap/grant/{grantId}/status` - Update a grant's status

### Token Management

- `POST /gnap/token/introspect` - Introspect a token
- `POST /gnap/token/revoke` - Revoke a token

### User Interaction

- Various endpoints for handling user consent and interaction flows

## Usage Examples

### Creating a Grant Request

```bash
curl -X POST http://localhost:8080/gnap/grant \
  -H "Content-Type: application/json" \
  -d '{
    "client": {
      "display": {
        "name": "Example Client"
      }
    },
    "access_token": [
      {
        "access": [
          {
            "type": "example_api",
            "actions": ["read", "write"]
          }
        ]
      }
    ],
    "interact": {
      "redirect": true,
      "user_code": true
    }
  }'
```

### Introspecting a Token

```bash
curl -X POST http://localhost:8080/gnap/token/introspect \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "token=YOUR_TOKEN_HERE"
```

## Project Structure

```
src/main/java/com/example/gnap/as/
├── config/                  # Configuration classes
├── controller/              # REST controllers
├── model/                   # Domain models
├── repository/              # Data repositories
├── service/                 # Business logic services
└── GnapAsApplication.java   # Main application class
```

## Database Schema

The application uses the following main entities:

- `GrantRequest` - Represents a grant request in the GNAP protocol
- `Client` - Represents a client application
- `AccessToken` - Represents an access token issued to a client
- `Interaction` - Represents a user interaction flow
- `Resource` - Represents a protected resource

## Security Considerations

- The server uses JSON Web Tokens (JWT) for access tokens
- Tokens have configurable lifetimes
- User interactions have timeouts for security
- All sensitive operations require proper authentication

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- GNAP Working Group for the protocol specification
- Spring Boot team for the excellent framework