FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn --batch-mode --no-transfer-progress verify

FROM tomcat:9.0-jdk17-temurin
COPY --from=build /app/target/dmn-coffee.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
