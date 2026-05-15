FROM amazoncorretto:21-al2023
WORKDIR /app
COPY target/analytics-service-*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]