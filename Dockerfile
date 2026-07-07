FROM amazoncorretto:21 AS builder

WORKDIR /app

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
#COPY global-bundle.pem global-bundle.pem

RUN chmod +x ./gradlew
RUN ./gradlew dependencies || true

COPY src src

RUN ./gradlew clean build -x test


FROM amazoncorretto:21-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/amumal-0.0.1-SNAPSHOT.jar app.jar
COPY --from=builder /app/global-bundle.pem global-bundle.pem

EXPOSE 3000
ENTRYPOINT ["java", "-jar", "app.jar"]