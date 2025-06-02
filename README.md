# 🥘 KitchenSink Spring Boot Application

A Spring Boot application demonstrating REST APIs, MongoDB integration, validation, security (JWT), and deployment with Azure App service - Github Actions

---

## 🔧 Tech Stack

- Java 21
- Spring Boot 3.3.0
- MongoDB
- Spring Security with JWT
- Maven
- Docker
- Deployed on Azure (App Service)

---

## 📦 Project Structure

src/
├─ main/
│ ├─ java/com/kitchensink/... # Source code
│ └─ resources/ # Configuration
└─ test/ # Tests

▶️ Running Locally
Pre-requisites
Java 21+
Maven 3.9+
MongoDB running
# Clean and build
mvn clean install

# Run the application
mvn spring-boot:run

# Run the application with docker
docker-compose up --build

# Swagger Endpoint
http://localhost:8080/swagger-ui/index.html


📈 Monitoring & Observability
Spring Boot Actuator is enabled.
Access actuator endpoints at: http://localhost:8080/actuator/health

☁️ Deployed on Azure
Application is connected to Github Actions and auto deployment is enabled using CI-CD