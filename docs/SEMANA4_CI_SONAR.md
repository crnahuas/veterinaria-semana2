# Semana 4: Jenkins + SonarQube

Este repositorio quedó preparado para la actividad "Implementando herramientas de análisis estático a través de CI/CD".

## Archivos relevantes

- `Dockerfile.mysql`: imagen para la base de datos MySQL.
- `SonarQube/docker-compose.yaml`: levanta Jenkins, SonarQube y MySQL para la actividad.
- `Jenkinsfile`: pipeline para compilar, probar y analizar el backend con SonarQube.
- `sonar-project.properties`: configuración base del análisis estático.

## Flujo propuesto

1. Levantar la infraestructura con `docker compose up -d` dentro de `SonarQube/`.
2. Acceder a Jenkins en `http://localhost:8082/jenkins`.
3. Acceder a SonarQube en `http://localhost:9000`.
4. Configurar en Jenkins un Pipeline apuntando al repositorio Git público.
5. Instalar en Jenkins los plugins `Git`, `Pipeline` y `SonarQube Scanner`.
6. Registrar en Jenkins el servidor SonarQube con nombre `SonarQube` y un token generado en SonarQube.
7. Ejecutar el pipeline para que el backend corra pruebas y publique el análisis estático.

Puertos por defecto del stack CI:

- `JENKINS_HTTP_PORT=8082`
- `SONARQUBE_HTTP_PORT=9000`
- `MYSQL_CI_PORT=3309`

Si necesitas cambiarlos:

```bash
JENKINS_HTTP_PORT=8083 SONARQUBE_HTTP_PORT=9001 MYSQL_CI_PORT=3310 docker compose up -d
```

## Evidencia sugerida para el PDF

- Pantalla de Jenkins con el job y la ejecución exitosa.
- Pantalla de SonarQube con el proyecto `veterinaria-backend`.
- Resumen de issues encontrados y su severidad.
- Correcciones realizadas sobre el backend.
