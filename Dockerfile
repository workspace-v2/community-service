FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x ./gradlew

# 의존성 다운로드 (캐시 있으면 재사용)
RUN --mount=type=cache,target=/root/.gradle ./gradlew dependencies

COPY src src

# CI, CD 단계 모두에서 테스트를 실행하였으므로 생략
RUN --mount=type=cache,target=/root/.gradle ./gradlew clean build -x test

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

 # 소유권 지정하며 파일 복사
COPY --from=builder --chown=spring:spring /app/build/libs/*.jar app.jar

# 사용자 지정
USER spring

EXPOSE 3000
ENTRYPOINT ["java", "-jar", "app.jar"]