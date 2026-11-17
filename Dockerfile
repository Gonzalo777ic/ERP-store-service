FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/valeerp-commercial-0.0.1-SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["sh", "-c", "exec java -Xmx256m -Dserver.port=${PORT:-8083} -jar /app/app.jar"]
