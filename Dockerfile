FROM maven:3.8.1-jdk-8 AS build
COPY src /home/app/src
COPY pom.xml /home/app

RUN mvn -f /home/app/pom.xml clean package -DskipTests=true

FROM openjdk:8-jdk-alpine
ARG BUILD_VERSION=${BUILD_VERSION}
ENV ENV_BUILD_VERSION=${BUILD_VERSION}
COPY --from=build /home/app/target/bizio-support-${BUILD_VERSION}-SNAPSHOT.jar /usr/local/lib/bizio-support-${BUILD_VERSION}-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom -jar /usr/local/lib/bizio-support-${ENV_BUILD_VERSION}-SNAPSHOT.jar