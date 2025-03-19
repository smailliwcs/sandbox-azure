FROM maven:3-eclipse-temurin-17 AS build
WORKDIR /root/app
COPY pom.xml ./
RUN mvn dependency:go-offline
COPY src/ src/
RUN mvn package

FROM eclipse-temurin:17-alpine AS run
RUN apk add --no-cache curl jq
WORKDIR /root/app
COPY --from=build /root/app/target/*.war app.war
CMD ["java", "-jar", "app.war"]
