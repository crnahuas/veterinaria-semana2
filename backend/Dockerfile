FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace/backend

COPY backend/pom.xml pom.xml
RUN mvn -q -DskipTests dependency:go-offline

COPY backend/src src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /workspace/backend/target/*.jar app.jar
COPY frontend /app/frontend

ENV DB_HOST=mysql
ENV DB_PORT=3306
ENV DB_NAME=mydatabase
ENV DB_USERNAME=myuser
ENV DB_PASSWORD=password
ENV SSL_ENABLED=false
ENV HTTPS_PORT=8384
ENV HTTP_PORT=8080

EXPOSE 8384

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
