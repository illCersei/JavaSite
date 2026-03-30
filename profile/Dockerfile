FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY profile/target/*.jar app.jar

RUN mkdir -p /app/logs

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "app.jar"]