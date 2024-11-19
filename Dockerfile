# 1. 베이스 이미지 설정
FROM eclipse-temurin:17-jdk-alpine

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. Gradle 빌드 후 생성된 JAR 파일을 컨테이너로 복사
COPY build/libs/dangdang-salon-service-*-SNAPSHOT.jar app.jar

# 4. JAR 파일 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]