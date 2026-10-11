# Build the Java web application
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package -DskipTests

# Run the WAR on the same Tomcat major version used locally
FROM tomcat:9.0-jdk17-temurin-jammy
ENV CATALINA_OPTS="-Xms128m -Xmx512m"
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/aishwarya_mart.war /usr/local/tomcat/webapps/aishwarya_mart.war
COPY railway-start.sh /usr/local/bin/railway-start.sh
RUN chmod +x /usr/local/bin/railway-start.sh
EXPOSE 8080
CMD ["/usr/local/bin/railway-start.sh"]
