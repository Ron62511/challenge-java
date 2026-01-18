# Multi-stage build para optimizar el tamaño de la imagen final

# Stage 1: Build - Compilar y empaquetar la aplicación
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copiar archivos de configuración de Maven (para cachear dependencias)
COPY pom.xml .

# Descargar dependencias (se cachean en esta capa si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar y empaquetar la aplicación
RUN mvn clean package -DskipTests

# Stage 2: Runtime - Imagen ligera solo con el JAR ejecutable
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Instalar wget para health check
RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*

# Crear usuario no-root para seguridad
RUN groupadd -r spring && useradd -r -g spring spring

# Copiar el JAR desde el stage de build
COPY --from=build /app/target/*.jar app.jar

# Cambiar ownership al usuario spring
RUN chown spring:spring app.jar

# Cambiar al usuario no-root
USER spring:spring

# Exponer el puerto de la aplicación
EXPOSE 8080

# Variables de entorno opcionales
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Health check para verificar que la aplicación está funcionando
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/swagger-ui.html || exit 1

# Ejecutar la aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
