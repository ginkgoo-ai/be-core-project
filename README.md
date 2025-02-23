# Ginkgoo Core Project Service

## Features

### Completed ✅

* Project Management 

### In Progress 🚧

## Tech Stack

* Java 21
* Spring Boot 3.x
* Spring Security
* PostgreSQL
* JWT

## Getting Started

```bash
git clone <repository-url>
cd core-project-service
```
mvn clean install
mvn spring-boot:run
```

## Health Check

Service health can be monitored at:

```bash
GET /health

# Response:
{
    "status": "UP",
}
```

## Configuration

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: ${AUTH_SERVER}/oauth2/jwks
          issuer-uri: ${AUTH_SERVER}

  datasource:
    url: ${POSTGRES_URL}
    username: ${POSTGRES_USER}
    password: ${POSTGRES_PASSWORD}
```

## Requirements

* JDK 21+
* PostgreSQL 14+
* Maven 3.8+

## License

This project is licensed under the **GNU Affero General Public License v3.0 (AGPL-3.0)**.  
See the [`LICENSE`](./LICENSE) file for details.

---

© 2025 Ginkgo Innovations. All rights reserved.

```