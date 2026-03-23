# Veterinaria Segura

Proyecto ajustado a la pauta de CDY2203 Exp 1 Semana 2 usando como referencia el backend entregado en `cdy2203-backend-2026-201-main.zip`.

## Tecnologías

- Spring Boot 3.3.6
- Spring Web
- Spring Security
- Spring Data JPA
- MySQL Driver
- JWT
- HTTPS con certificado local PKCS12

## Base de datos MySQL

La configuración principal del backend usa MySQL en `src/main/resources/application.properties`.

Variables por defecto:

- `DB_HOST=localhost`
- `DB_PORT=3308`
- `DB_NAME=mydatabase`
- `DB_USERNAME=myuser`
- `DB_PASSWORD=password`

Puedes cambiar el string de conexión editando `src/main/resources/application.properties` o sobreescribiendo `DB_URL`.

## Dockerfile MySQL

Archivo requerido por la pauta: `Dockerfile`

Comandos sugeridos:

```bash
docker build -t veterinaria-mysql .
docker run -d --name veterinaria-mysql -p 3308:3306 veterinaria-mysql
```

Opcionalmente puedes usar `docker-compose.yml`:

```bash
docker compose up -d
```

## HTTPS

La aplicación arranca sobre HTTPS:

- `https://localhost:8443`
- `http://localhost:8080` redirige a HTTPS

Certificado local:

- Keystore: `src/main/resources/veterinaria-keystore.p12`
- Alias: `veterinaria-https`

Como el certificado es autofirmado, el navegador mostrará una advertencia la primera vez.

## Usuarios de prueba

- `recepcionista / vet123`
- `veterinario / vet123`
- `admin / admin123`

## APIs implementadas

### Públicas

- `POST /api/login`
  - Body:
```json
{
  "username": "admin",
  "password": "admin123"
}
```
  - Respuesta:
```json
{
  "token": "jwt",
  "username": "admin",
  "role": "ADMIN"
}
```

### Privadas

- `GET /api/pacientes`
  - Roles: `RECEPCIONISTA`, `VETERINARIO`, `ADMIN`
  - Header: `Authorization: Bearer <token>`

- `POST /api/pacientes`
  - Roles: `RECEPCIONISTA`, `ADMIN`
  - Header: `Authorization: Bearer <token>`
  - Body:
```json
{
  "nombre": "Luna",
  "especie": "Perro",
  "raza": "Beagle",
  "edad": 4,
  "nombreDueno": "Carla Soto"
}
```

- `GET /api/pacientes/{id}`
  - Roles: `RECEPCIONISTA`, `VETERINARIO`, `ADMIN`
  - Parámetro: `id`

- `GET /api/citas`
  - Roles: `RECEPCIONISTA`, `VETERINARIO`, `ADMIN`
  - Header: `Authorization: Bearer <token>`

- `POST /api/citas`
  - Roles: `RECEPCIONISTA`, `ADMIN`
  - Header: `Authorization: Bearer <token>`
  - Body:
```json
{
  "pacienteId": 1,
  "fechaHora": "2026-03-30T10:30:00",
  "motivo": "Control general",
  "veterinarioAsignado": "Dra. Pérez"
}
```

## Frontend

Archivos principales:

- `src/main/resources/static/index.html`
- `src/main/resources/static/pacientes.html`
- `src/main/resources/static/citas.html`

Comportamiento por rol:

- `RECEPCIONISTA` y `ADMIN` pueden registrar pacientes y programar citas.
- `VETERINARIO` puede consultar pacientes y citas, pero no crear registros.

## Pruebas

Perfil de test en `src/main/resources/application-test.properties` usando H2 para pruebas automáticas.

Ejecutar:

```bash
./mvnw test
```
