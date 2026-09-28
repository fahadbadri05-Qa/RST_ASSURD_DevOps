FROM maven:3.9-eclipse-temurin-17

WORKDIR /app

# Cache dependencies separately so code changes don't force a re-download
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

ENTRYPOINT ["mvn", "test"]
