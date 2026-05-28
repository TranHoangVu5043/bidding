# ── Build stage ────────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk-noble AS builder
WORKDIR /build

RUN apt-get update && \
    apt-get install -y --no-install-recommends maven && \
    rm -rf /var/lib/apt/lists/*

# Cache dependency downloads separately from source compilation
COPY pom.xml .
RUN mvn dependency:go-offline --no-transfer-progress

COPY src ./src
RUN mvn package -DskipTests --no-transfer-progress

# ── Run stage ──────────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre-noble
WORKDIR /app

COPY --from=builder /build/target/bidding-1.0-server.jar app.jar

EXPOSE 8080
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
