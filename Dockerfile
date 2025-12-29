####
# Multi-stage Dockerfile for building and running the Quarkus application in JVM mode
#
# Build and run the container using:
#
# docker build -t quarkus/submit-jvm .
# docker run -i --rm -p 8080:8080 quarkus/submit-jvm
#
###

# Stage 1: Build the application
FROM registry.access.redhat.com/ubi8/openjdk-21:1.20 AS builder

USER root
WORKDIR /build

# Copy Maven wrapper and pom.xml first for better layer caching
COPY --chown=185 mvnw pom.xml ./
COPY --chown=185 .mvn .mvn

# Download dependencies (cached if pom.xml unchanged)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY --chown=185 src src

# Build the application
RUN ./mvnw package -DskipTests -B

# Stage 2: Runtime image
FROM registry.access.redhat.com/ubi8/openjdk-21:1.20

ENV LANGUAGE='en_US:en'

# We make four distinct layers so if there are application changes the library layers can be re-used
COPY --from=builder --chown=185 /build/target/quarkus-app/lib/ /deployments/lib/
COPY --from=builder --chown=185 /build/target/quarkus-app/*.jar /deployments/
COPY --from=builder --chown=185 /build/target/quarkus-app/app/ /deployments/app/
COPY --from=builder --chown=185 /build/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
