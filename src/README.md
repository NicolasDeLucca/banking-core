# Banking Core

Backend inicial en **Java + Spring Boot** para el proyecto portfolio.

## Nombre del proyecto
El nombre definido para este backend es **Banking Core** (basado en la conversación de diseño, donde se propuso y recomendó “Banking Core” como dominio principal).

## Stack
- Java 21
- Spring Boot 3
- Spring Web
- Spring Security
- Spring Data JPA
- H2 (dev)
- JWT

## Run
```bash
mvn spring-boot:run
```

## Test
```bash
mvn test
```

## Endpoints iniciales
- `POST /api/auth/register`
- `POST /api/auth/login`

### Ejemplo register
```json
{
  "email": "user@example.com",
  "password": "secret123"
}
```

## Estructura modular inicial
- `auth`: casos de uso de autenticación
- `users`: modelo y repositorio de usuarios
- `shared/error`: errores de dominio y handler global
- `shared/security`: configuración base de seguridad + JWT service