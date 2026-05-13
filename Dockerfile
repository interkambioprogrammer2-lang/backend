# Etapa de construcción
FROM maven:3.8-openjdk-17 AS build
WORKDIR /app

# Copiar POM y descargar dependencias
COPY pom.xml .
RUN mvn dependency:resolve dependency:resolve-plugins

# Copiar código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="Interkambio"
LABEL description="API Backend para Control de Libros en Ferias"

WORKDIR /app

# Crear usuario no-root
RUN addgroup -g 1000 appuser && adduser -D -u 1000 -G appuser appuser

# Copiar el JAR (usa comodín para cualquier nombre)
COPY --from=build /app/target/*.jar app.jar
RUN chown appuser:appuser app.jar

USER appuser

EXPOSE 8080

# Health check (opcional, puede fallar si no tienes /actuator/health)
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]