# Build with the Java version declared in pom.xml.
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src src
RUN ./mvnw -B package -DskipTests

# A JRE image keeps the production image small.
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar

# Render supplies PORT.  Limit the JVM so the service remains within its 512 MB plan.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60 -XX:InitialRAMPercentage=25 -XX:+UseSerialGC -Djava.security.egd=file:/dev/./urandom"
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
