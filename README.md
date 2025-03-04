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

This project is available under a **dual licensing model**:

- **Open Source License (AGPL-3.0)**:  
  This software is licensed under the **GNU Affero General Public License v3.0 (AGPL-3.0)**.  
  See the **[LICENSE](./LICENSE)** file for details.

- **Commercial License**:  
  If you wish to use this software **without complying with AGPL-3.0**  
  or require a **proprietary license** with different terms,  
  please contact **[license@ginkgoo.ai](mailto:license@ginkgoo.ai)**.

## Contributing

We welcome contributions!
Please read our **[Contributing Guide](./CONTRIBUTING.md)** before submitting issues or pull requests.

## Code of Conduct

To foster a welcoming and inclusive environment, we adhere to our **[Code of Conduct](./CODE_OF_CONDUCT.md)**.
All contributors are expected to follow these guidelines.

## Contributor License Agreement (CLA)

Before making any contributions, you must agree to our **[CLA](./CLA.md)**.
This ensures that all contributions align with the project’s **dual licensing model**.

If you have any questions, contact **[license@ginkgoo.ai](mailto:license@ginkgoo.ai)**.

---

© 2025 Ginkgo Innovations. All rights reserved.