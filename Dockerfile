FROM maven:3.8.4-eclipse-temurin-17
WORKDIR learnjavabot/
ADD pom.xml .
RUN mvn verify --fail-never
ADD ./ .
RUN mvn package
FROM eclipse-temurin:17
RUN apt-get update && apt-get install -y wait-for-it
WORKDIR /root/
COPY --from=0 /learnjavabot/target/*.jar .
