# stage 1: build the jar with maven and java 25
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# copy pom.xml first and download dependencies, docker caches this step
# so they are only downloaded again when pom.xml changes
COPY pom.xml .
RUN mvn -q dependency:go-offline

# then copy the code and build, tests run separately because they need docker themselves
COPY src ./src
RUN mvn -q package -DskipTests

# stage 2: a small image with only java and the jar, no maven or source code
FROM eclipse-temurin:25-jre
WORKDIR /app

# run as a normal user instead of root
RUN useradd --system app
USER app

COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]