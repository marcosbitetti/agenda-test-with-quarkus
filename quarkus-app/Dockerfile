FROM maven:3.9.4-eclipse-temurin-17

WORKDIR /workspace

ENV MAVEN_CONFIG=/var/maven/.m2

RUN mkdir -p /var/maven/.m2/repository && chmod -R 777 /var/maven

# For development we rely on a mounted project directory and run quarkus:dev
CMD ["bash", "-lc", "mvn -DskipTests quarkus:dev -Dquarkus.http.host=0.0.0.0"]
