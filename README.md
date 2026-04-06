# Veterinaria Segura

El repositorio quedó reorganizado para separar físicamente las capas:

- `backend/`: API independiente en Spring Boot.
- `frontend/`: cliente web estático que consume la API como consumidor externo.
- `docker-compose.yml`: levanta MySQL, backend y frontend.

## Estructura

```text
.
├── backend
├── frontend
├── docs
├── docker-compose.yml
└── Dockerfile.mysql
```

## Docker Compose

Con Docker Compose se levantan los 3 servicios:

- `mysql`
- `backend`
- `frontend`

Ejecuta:

```bash
docker compose up --build -d
```

Luego abre:

- `https://localhost:8443/login`

El frontend en Docker usa proxy interno hacia el backend, por lo que no necesitas configurar ninguna URL manualmente. Si entras por `http://localhost:8080`, redirige a HTTPS.

Para detener todo:

```bash
docker compose down
```

## Backend

El backend expone únicamente endpoints API y ya no sirve HTML, CSS ni JavaScript embebidos.

- Aplicación base: `https://localhost:8384`
- Login público: `POST /api/login`
- Endpoints protegidos: `/api/pacientes`, `/api/citas`, `/greetings`
- Configuración principal: `backend/src/main/resources/application.properties`

Usuarios de prueba:

- `recepcionista / vet123`
- `veterinario / vet123`
- `admin / admin123`

Ejecución desde la raíz del repositorio:

```bash
./mvnw spring-boot:run
```

Pruebas:

```bash
./mvnw test
```

Alternativa equivalente dentro de `backend/`:

```bash
cd backend
./mvnw spring-boot:run
```

## Frontend

El frontend quedó desacoplado y puede levantarse con cualquier servidor estático. Usa el mismo origen del sitio o un proxy hacia el backend.

Cuando levantas Spring Boot directamente, el mismo backend también sirve las páginas en rutas amigables:

- `/`
- `/login`
- `/pacientes`
- `/citas`

Ejemplo con servidor estático local:

```bash
cd frontend
python3 -m http.server 5500
```

Luego abre:

- `http://localhost:5500`

## Base de datos MySQL

Variables por defecto:

- `DB_HOST=localhost`
- `DB_PORT=3308`
- `DB_NAME=mydatabase`
- `DB_USERNAME=myuser`
- `DB_PASSWORD=password`

Con Docker Compose ya se levanta junto con backend y frontend.

## Documentación

- API: `docs/API_DOCUMENTACION.md`
- Backend: `backend/README.md`
- Frontend: `frontend/README.md`
