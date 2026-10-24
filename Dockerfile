FROM eclipse-temurin:27-jre
RUN apt-get update && apt-get install -y wait-for-it
WORKDIR /root/
COPY target/*.jar app.jar
