# Build with the Java version declared in pom.xml.
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src src
RUN ./mvnw -B clean package -DskipTests

# A JRE image keeps the production image small.
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar

# Render routes public traffic to port 10000 for this Docker service.
# Limit the JVM so the service remains within a small Render instance.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60 -XX:InitialRAMPercentage=25 -XX:+UseSerialGC -Djava.security.egd=file:/dev/./urandom"
EXPOSE 10000

ENTRYPOINT ["java", "-Dserver.port=10000", "-jar", "/app/app.jar"]
