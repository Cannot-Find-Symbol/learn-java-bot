FROM eclipse-temurin:17
RUN apt-get update && apt-get install -y wait-for-it
WORKDIR /root/
COPY target/*.jar app.jar
