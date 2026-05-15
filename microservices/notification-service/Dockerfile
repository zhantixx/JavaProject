FROM amazoncorretto:21-al2023
WORKDIR /app
COPY target/notification-service-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]