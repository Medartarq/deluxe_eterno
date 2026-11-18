FROM eclipse-temurin:26-jdk AS build

RUN apt-get update \
    && apt-get install --yes --no-install-recommends maven \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace
COPY pom.xml .
RUN mvn --batch-mode --quiet dependency:go-offline
COPY src src
RUN mvn --batch-mode --no-transfer-progress -DskipTests package

FROM eclipse-temurin:26-jdk

WORKDIR /app
RUN useradd --system --create-home spring
COPY --from=build /workspace/target/*.jar app.jar

USER spring
ENV JAVA_TOOL_OPTIONS="-XX:InitialRAMPercentage=30 -XX:MaxRAMPercentage=70"
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "app.jar"]
