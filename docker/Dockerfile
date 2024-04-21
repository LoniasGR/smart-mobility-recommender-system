FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR smart-mobility
COPY .mvn .mvn
COPY src src
COPY mvnw mvnw
COPY pom.xml pom.xml
RUN ./mvnw package

FROM eclipse-temurin:21-alpine
RUN mkdir /opt/app
COPY --from=builder /smart-mobility/target/smart-mobility-demo-0.0.1-SNAPSHOT.jar /opt/app/app.jar
CMD ["java", "-jar", "/opt/app/app.jar"]

