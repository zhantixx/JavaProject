FROM amazoncorretto:21-al2023
WORKDIR /app
COPY target/ai-service-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]