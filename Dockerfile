# 1. Java 21 런타임 환경 사용 (OpenJDK 21)
FROM eclipse-temurin:21-jre

# 2. JAR 파일을 이미지에 복사
COPY build/libs/*.jar app.jar

# 3. 컨테이너 시작 시 실행할 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]