# Stage 1: Build & Layer Extraction
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /workspace

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw clean package -DskipTests

# Extract Spring Boot layers
RUN java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted

# Stage 2: Minimal Production Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /application

RUN addgroup -S echolife && adduser -S echolife -G echolife
USER echolife:echolife

COPY --from=builder /workspace/target/extracted/dependencies/ ./
COPY --from=builder /workspace/target/extracted/spring-boot-loader/ ./
COPY --from=builder /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=builder /workspace/target/extracted/application/ ./

EXPOSE 8082

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
