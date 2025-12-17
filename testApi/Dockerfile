FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Кэшируем зависимости по pom.xml
COPY pom.xml .
COPY auth/pom.xml auth/pom.xml
COPY testApi/pom.xml testApi/pom.xml
COPY twitchService/pom.xml twitchService/pom.xml
COPY common/pom.xml common/pom.xml
COPY newYearService/pom.xml newYearService/pom.xml
RUN mvn -B -q dependency:go-offline

# Копируем код проекта
COPY . .

# Собираем только testApi модуль
RUN mvn -B -q clean package -DskipTests -pl testApi -am

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/testApi/target/*.jar app.jar

RUN mkdir -p /app/logs

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "app.jar"]