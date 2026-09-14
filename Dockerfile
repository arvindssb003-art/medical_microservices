FROM maven:3.9.9-eclipse-temurin-21 AS build

ARG SERVICE_DIR
WORKDIR /workspace

COPY ${SERVICE_DIR}/ /workspace/
RUN mvn -q -DskipTests package \
    && find target -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' -print -quit \
       | xargs -r cp -t /workspace/

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
