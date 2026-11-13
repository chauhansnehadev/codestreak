# Stage 1: Build application with Maven
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime environment with Apache Tomcat 10.1
FROM tomcat:10.1-jdk17-temurin

# Remove default ROOT application
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy WAR as both ROOT.war and codestreak.war so both / and /codestreak/ work seamlessly
COPY --from=builder /build/target/codestreak.war /usr/local/tomcat/webapps/ROOT.war
COPY --from=builder /build/target/codestreak.war /usr/local/tomcat/webapps/codestreak.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
