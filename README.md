# EchoLife Safety & Risk Microservice (`s4-echolife-safety-risk`)

High-throughput Spring Boot service responsible for real-time risk assessment, 
guardrail evaluation, and asynchronous event escalations.

---

## 📂 Project Architecture & Directory Structure

```text
s4-echolife-safety-risk/
├── .mvn/wrapper/                  # Maven wrapper binaries & properties
├── src/
│   ├── main/
│   │   ├── java/com/echolife/s4echolifesafetyrisk/
│   │   │   ├── config/            # Infrastructure & bean configurations (Kafka, Redis, JPA)
│   │   │   ├── controller/        # REST API endpoints (SafetyController)
│   │   │   ├── dto/               # Data Transfer Objects (Requests & Responses)
│   │   │   ├── entity/            # JPA Database entities (SafetyEvent, SafetyPolicy)
│   │   │   ├── repository/        # Spring Data repositories
│   │   │   ├── service/           # Business logic & policy evaluation engines
│   │   │   └── S4EcholifeSafetyRiskApplication.java  # Main application entry point
│   │   └── resources/
│   │       ├── db/migration/      # Flyway SQL migration scripts (V1__init_safety_tables.sql)
│   │       └── application.yaml   # Central application properties
│   └── test/                      # Unit & integration tests
├── docker-compose.yml             # Local orchestration (Postgres, Redis, Kafka)
├── pom.xml                        # Maven dependencies & build definitions
└── README.md                      # Service documentation
🛠 Tech Stack
Language/Runtime: Java 26

Framework: Spring Boot 3.3.3

Database: PostgreSQL (Schema versioned via Flyway)

In-Memory Cache: Redis

Messaging: Apache Kafka (KRaft mode, topic: safety-events)

Containerization: Docker Compose

🚀 Quick Start
1. Start Infrastructure
Start the required PostgreSQL, Redis, and Kafka containers:

Bash
docker compose up -d
2. Run Application
Run the service locally on port 8082:

Bash
./mvnw spring-boot:run
📡 API Reference
Evaluate Safety & Risk
URL: POST /api/v1/safety/evaluate

Content-Type: application/json

Sample Request Body:

JSON
{
  "sessionId": "session-101",
  "userId": "user-456",
  "content": "Sample prompt or message to evaluate"
}
Response:

Evaluates input against stored safety rules.

Persists the evaluation record into PostgreSQL.

Emits high-risk escalations asynchronously to Kafka.


The very last line to paste is the sentence:
`* Emits high-risk escalations asynchronously to Kafka.`