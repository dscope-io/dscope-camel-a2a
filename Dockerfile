FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -e -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/target/camel-a2a.jar /app/camel-a2a.jar
COPY routes /app/routes
EXPOSE 8080 8081
ENTRYPOINT ["java","-jar","/app/camel-a2a.jar"]
