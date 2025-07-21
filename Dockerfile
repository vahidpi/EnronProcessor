# Stage 1: Build the application
FROM openjdk:17-jdk-slim AS build

WORKDIR /build

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:resolve

COPY src src
RUN ./mvnw package -DskipTests

# Stage 2: Create runtime image
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /build/target/*.jar EnronProcessor.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "EnronProcessor.jar"]
