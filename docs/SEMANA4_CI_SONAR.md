# Semana 4: Jenkins + SonarQube

Este repositorio quedó preparado para la actividad "Implementando herramientas de análisis estático a través de CI/CD".

## Archivos relevantes

- `Dockerfile.mysql`: imagen para la base de datos MySQL.
- `SonarQube/docker-compose.yaml`: levanta Jenkins, SonarQube y MySQL para la actividad.
- `Jenkinsfile`: pipeline para compilar, probar y analizar el backend con SonarQube.
- `sonar-project.properties`: configuración base del análisis estático.

## Flujo propuesto

1. Levantar la infraestructura con `docker compose up -d` dentro de `SonarQube/`.
2. Configurar en Jenkins un Pipeline apuntando al repositorio Git público.
3. Instalar en Jenkins los plugins `Git`, `Pipeline` y `SonarQube Scanner`.
4. Registrar en Jenkins el servidor SonarQube con nombre `SonarQube` y un token generado en SonarQube.
5. Ejecutar el pipeline para que el backend corra pruebas y publique el análisis estático.

## Evidencia sugerida para el PDF

- Pantalla de Jenkins con el job y la ejecución exitosa.
- Pantalla de SonarQube con el proyecto `veterinaria-backend`.
- Resumen de issues encontrados y su severidad.
- Correcciones realizadas sobre el backend.
