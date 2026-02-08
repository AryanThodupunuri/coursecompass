# syntax=docker/dockerfile:1

FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy only backend sources to keep build context smaller
COPY backend/pom.xml backend/pom.xml
COPY backend/src backend/src

RUN mvn -f backend/pom.xml -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /workspace/backend/target/backend-0.1.0-SNAPSHOT.jar /app/app.jar

ENV PORT=8080
EXPOSE 8080

CMD ["sh", "-c", "java -Dserver.port=${PORT} -jar /app/app.jar"]
