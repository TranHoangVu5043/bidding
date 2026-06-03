#  Build stage
FROM eclipse-temurin:25-jdk-noble AS builder
WORKDIR /build

ENV MAVEN_VERSION=3.9.9
RUN apt-get update && apt-get install -y --no-install-recommends wget ca-certificates && \
    wget -q https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz -O /tmp/maven.tar.gz && \
    tar -xzf /tmp/maven.tar.gz -C /opt && \
    ln -s /opt/apache-maven-${MAVEN_VERSION}/bin/mvn /usr/local/bin/mvn && \
    rm /tmp/maven.tar.gz && rm -rf /var/lib/apt/lists/*

COPY pom.xml .
COPY server/pom.xml server/
RUN mvn dependency:go-offline -pl server --no-transfer-progress

COPY server/src ./server/src
RUN mvn package -pl server -DskipTests --no-transfer-progress

#  Run stage
FROM eclipse-temurin:25-jre-noble
WORKDIR /app

COPY --from=builder /build/server/target/bidding-server-1.0-server.jar app.jar

EXPOSE 8080
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
