# syntax=docker/dockerfile:1
# ---------- Etapa 1: compilacion con Maven + JDK 24 ----------
FROM maven:3.9-eclipse-temurin-24 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q clean package -DskipTests

# ---------- Etapa 2: runtime JRE 24 ----------
FROM eclipse-temurin:24-jre

WORKDIR /app
RUN useradd -r -u 1001 techzone
COPY --from=build /app/target/*.war app.war
USER techzone

EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.war"]
