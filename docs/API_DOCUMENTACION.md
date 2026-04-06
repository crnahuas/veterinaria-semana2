# Documentación breve de APIs - Veterinaria

Esta documentación corresponde al backend desacoplado ubicado en `backend/`.

## Usuarios iniciales
- recepcionista / vet123
- veterinario / vet123
- admin / admin123

## Autenticación pública
### POST /api/login
- Método: POST
- Body JSON:
```json
{
  "username": "admin",
  "password": "admin123"
}
```
- Respuesta: token JWT, username y role.

## APIs privadas
### GET /api/pacientes
- Lista pacientes.
- Requiere header `Authorization: Bearer <token>`.
- Roles: RECEPCIONISTA, VETERINARIO, ADMIN.

### POST /api/pacientes
- Registra paciente.
- Body JSON:
```json
{
  "nombre": "Luna",
  "especie": "Perro",
  "raza": "Beagle",
  "edad": 4,
  "nombreDueno": "Carla Soto"
}
```
- Roles: RECEPCIONISTA, ADMIN.

### GET /api/pacientes/{id}
- Obtiene paciente por id.
- Roles: RECEPCIONISTA, VETERINARIO, ADMIN.

### GET /api/citas
- Lista citas.
- Roles: RECEPCIONISTA, VETERINARIO, ADMIN.

### POST /api/citas
- Programa una cita.
- Body JSON:
```json
{
  "pacienteId": 1,
  "fechaHora": "2026-03-25T10:30:00",
  "motivo": "Control general",
  "veterinarioAsignado": "Dr. Pérez"
}
```
- Roles: RECEPCIONISTA, ADMIN.

### GET /greetings
- Endpoint protegido de prueba.
- Requiere JWT.

## Conexión a base de datos
La conexión principal se configura en `backend/src/main/resources/application.properties`.
También puede cambiarse por variables de entorno:
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

## CORS para frontend externo
El backend permite consumo desde un frontend separado. Los orígenes permitidos se configuran con:

- `CORS_ALLOWED_ORIGIN_PATTERNS`

Valor por defecto:

- `http://localhost:*`
- `http://127.0.0.1:*`
- `https://localhost:*`
- `https://127.0.0.1:*`

## Docker entregado
- `Dockerfile.mysql`: crea la base de datos MySQL.
- `Dockerfile`: alias mantenido para la imagen MySQL requerida por la pauta.
- `apache/Dockerfile`: crea el proxy Apache HTTPS.
- `docker-compose.yml`: levanta MySQL para el backend.
