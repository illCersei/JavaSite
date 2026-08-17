FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S app && adduser -S app -G app \
    && mkdir -p /app/logs && chown -R app:app /app

COPY wallet/target/*.jar app.jar
RUN chown app:app app.jar

USER app
EXPOSE 8000

ENV JDK_JAVA_OPTIONS="-XX:MaxRAMPercentage=75.0"

HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
    CMD wget --no-verbose --tries=1 -O /dev/null \
        "http://${METRICS_USERNAME}:${METRICS_PASSWORD}@127.0.0.1:${MONITORING_PORT}/actuator/health" \
        || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
