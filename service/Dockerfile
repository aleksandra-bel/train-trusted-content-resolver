FROM eclipse-temurin:21-jre

# Install native crypto dependency
RUN apt-get update \
 && apt-get install -y libsodium23 \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY target/*.jar app.jar

EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]


