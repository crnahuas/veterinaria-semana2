# Frontend

Cliente web estático que también puede ser servido por Spring Boot en las rutas `/`, `/login`, `/pacientes` y `/citas`.

## Docker

Desde la raíz del proyecto:

```bash
docker compose up --build -d
```

Luego abre:

- `https://localhost:8443/login`

En este modo, el frontend usa proxy interno hacia el backend y `http://localhost:8080` redirige a HTTPS.

## Ejecutar

Con cualquier servidor estático. Ejemplo:

```bash
cd frontend
python3 -m http.server 5500
```

Luego abre:

- `http://localhost:5500`

## Configuración

- URL por defecto del backend: el mismo origen del sitio donde se sirve el frontend
- El token JWT, rol, usuario y URL del backend se guardan en `localStorage`

## Flujo

1. Abrir `/login` o `login.html`
2. Iniciar sesión
3. Consumir pacientes y citas vía API protegida
