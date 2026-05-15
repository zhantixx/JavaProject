FROM amazoncorretto:21-al2023
WORKDIR /app
COPY target/task-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]