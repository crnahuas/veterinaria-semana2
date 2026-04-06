# Backend

Proyecto Spring Boot separado del frontend y expuesto como API independiente.

Además, cuando se ejecuta el backend, también publica el frontend del repositorio en rutas amigables como `/` y `/login`.

## Ejecutar

```bash
./mvnw spring-boot:run
```

La API queda disponible por defecto en:

- `https://localhost:8384`
- `http://localhost:8080` redirige a HTTPS

## Pruebas

```bash
./mvnw test
```

## Configuración importante

- Base de datos: `src/main/resources/application.properties`
- Certificado HTTPS: `src/main/resources/veterinaria-keystore.p12`
- CORS para frontend externo: `CORS_ALLOWED_ORIGIN_PATTERNS`
