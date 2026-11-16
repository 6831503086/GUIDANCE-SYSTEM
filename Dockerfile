# ===== Stage 1: BUILD =====
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B
COPY src src
RUN ./mvnw clean package -DskipTests -B

# ===== Stage 2: RUN =====
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY deploy-seed /app/data
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]