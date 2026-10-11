# Build the Java Servlet application as a WAR.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package -DskipTests

# Run the WAR on the same Tomcat major version used locally.
FROM tomcat:9.0-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/aishwarya_mart.war /usr/local/tomcat/webapps/aishwarya_mart.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
