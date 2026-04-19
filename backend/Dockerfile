# syntax=docker/dockerfile:1
FROM eclipse-temurin:26-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw -q -DskipTests package

FROM eclipse-temurin:26-jre-alpine
WORKDIR /app
ENV JAVA_OPTS="-Xmx256m -Xms64m"
COPY --from=build /app/target/zero-backend-*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
