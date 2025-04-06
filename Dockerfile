# --- Build Stage ---
FROM eclipse-temurin:17-jdk AS build

WORKDIR /tmp/build

# Copy project files
COPY . .

# Make Gradle wrapper executable (in case it's not)
RUN chmod +x ./gradlew

# Build the fat jar and skip signing
RUN ./gradlew shadowJar --no-daemon -Psigning.skip=true

# --- Runtime Stage ---
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the fat jar
COPY --from=build /tmp/build/build/libs/kotlin-htmx-all.jar .

# Copy env file
COPY .env.default .

# Configure runtime
ENV TZ="America/Guatemala"
EXPOSE 8080

CMD ["java", "-jar", "kotlin-htmx-all.jar"]
