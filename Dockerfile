# --- Build Stage ---
FROM eclipse-temurin:17-jdk AS build

WORKDIR /tmp/build


COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .


RUN ./gradlew dependencies || true


COPY . .


RUN ./gradlew shadowJar --no-daemon -PenableSigning=false


FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /tmp/build/build/libs/kotlin-htmx-all.jar .
COPY .env.default .

ENV TZ="America/Guatemala"
EXPOSE 8085

CMD ["java", "-jar", "kotlin-htmx-all.jar"]
