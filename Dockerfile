FROM maven:3.9.9-eclipse-temurin-21

WORKDIR /app

COPY . .

RUN mvn -DskipTests package

CMD ["java","-jar","target/maintenance-0.0.1-SNAPSHOT.jar"]