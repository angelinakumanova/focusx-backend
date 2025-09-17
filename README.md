# FocusX Backend

## 📋 **Overview**  
This repository contains the **backend** of **FocusX**, implemented as **three microservices** within a single repository.  
The backend combines a Pomodoro timer, goal tracking, rewards system and notification service, built with **Java 21**, **Spring Boot**, and **MongoDB**.  
The microservices communicate asynchronously via **Apache Kafka** for real-time updates.
API documentation is provided through **Swagger** for easy testing and exploration.

## ✨ **Microservices**  
- **User Service:** Handles user management and authentication (JWT).  
- **Goal Service:** Manages goals, tracking progress, and rewards.  
- **Session Service:** Manages Pomodoro timer sessions.
- **Notification Service:** Sends notifications to users to keep them informed and engaged.

## ✨ **Features**  
- ⚙️ Microservices architecture bundled in one repo for ease of management  
- 🗄️ MongoDB for data persistence
- 💾 Redis for fast in-memory caching for performance optimization
- 🔐 JWT-based authentication and authorization  
- 📨 Apache Kafka for event-driven communication between services
- 📚 **Swagger** UI for interactive API documentation

## 🛠️ **Tech Stack**  
- **Java 21**  
- **Spring Boot**  
- **MongoDB**
- **Redis**
- **Apache Kafka** (Confluent Cloud)  
- **Swagger / OpenAPI**  
- **Docker**

