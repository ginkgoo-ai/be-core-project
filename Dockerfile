FROM maven:3.9-amazoncorretto-23 AS builder
ARG GITHUB_USER
ARG GITHUB_TOKEN

WORKDIR /app
COPY pom.xml ./
COPY settings.xml ./
COPY src ./src

RUN mvn clean install -U -s settings.xml && \
    mvn package -Dmaven.test.skip=true -s settings.xml

FROM openjdk:23-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
COPY integration/opentelemetry-javaagent-2.15.0.jar ./opentelemetry-javaagent-2.15.0.jar

CMD ["java", "-Xms128m", "-Xmx1024m", "-javaagent:opentelemetry-javaagent-2.15.0.jar", "-jar", "app.jar"]
