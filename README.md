# CourseCompass

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java" />
  <img src="https://img.shields.io/badge/Spring_Boot-3-green?style=for-the-badge&logo=springboot" />
  <img src="https://img.shields.io/badge/PostgreSQL-Neon-blue?style=for-the-badge&logo=postgresql" />
  <img src="https://img.shields.io/badge/Chrome_Extension-MV3-yellow?style=for-the-badge&logo=googlechrome" />
  <img src="https://img.shields.io/badge/Docker-Containerized-blue?style=for-the-badge&logo=docker" />
  <img src="https://img.shields.io/badge/Render-Deployment-purple?style=for-the-badge&logo=render" />
  <img src="https://img.shields.io/badge/RateMyProfessors-GraphQL-red?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Reddit-JSON-orange?style=for-the-badge&logo=reddit" />
</p>

---

## Table of Contents
- [Overview](#overview)
- [Why I Built This](#why-i-built-this)
- [Architecture](#architecture)
- [Technologies Used](#technologies-used)
- [Project Structure](#project-structure)
- [Installation and Setup](#installation-and-setup)
- [Data Flow](#data-flow)
- [License](#license)
- [Contact](#contact)

---

## Overview
CourseCompass is a full-stack intelligence engine designed to eliminate information asymmetry during the course registration process at the University of Virginia. The system consists of a Chrome extension that injects real-time professor sentiment and workload analytics directly into university course catalog interfaces such as Lou’s List.

Instead of relying on brittle, high-maintenance scrapers, the system uses a distributed architecture that mimics internal GraphQL queries and public JSON endpoints. This allows CourseCompass to aggregate data from RateMyProfessors and Reddit at scale without requiring expensive proxy services.

---

## Why I Built This
I built CourseCompass to solve a high-impact, practical problem faced by my peers while demonstrating my ability to handle complex engineering challenges such as asynchronous data orchestration and web scraping at scale.

The project pushed me beyond simple utility scripts into designing a resilient system capable of managing cross-origin communication, high-latency external requests, and rate limits. I implemented core system design principles like introducing a persistence layer to cache results and drastically reduce redundant external traffic.

This mirrors the technical consolidation and API integration work I’ve done during my software engineering internships, applied to a community-focused academic tool.

---

## Architecture
CourseCompass follows a client–server architecture optimized for performance and low-latency feedback:

- **Client (Chrome Extension)**  
  A Manifest V3 extension that performs DOM manipulation to inject UI elements and communicates with the backend API.

- **Backend (Spring Boot)**  
  A Java-based API responsible for orchestrating parallel scraping tasks, aggregating results, and managing the data lifecycle.

- **Data Layer (PostgreSQL)**  
  A cloud-hosted database used to cache course profiles, enforce rate limits, and improve response times.

---

## Technologies Used

**Frontend**
- JavaScript (Vanilla)
- CSS Injection
- Chrome Extension API (Manifest V3)

**Backend**
- Java 17
- Spring Boot
- Jsoup (HTML & JSON parsing)

**Infrastructure**
- Docker
- Render
- Neon (PostgreSQL)

**External APIs**
- RateMyProfessors (GraphQL)
- Reddit (Public JSON)

---

## Project Structure
```bash
/extension
├── manifest.json
├── content scripts (DOM injection)
└── extension assets

/backend
├── controllers
├── services (scraping + orchestration)
├── JPA entities
└── repositories

Dockerfile
render.yaml
```

---

## Installation and Setup

### Chrome Extension Setup
1. Open Chrome and navigate to `chrome://extensions`.
2. Enable **Developer mode**.
3. Click **Load unpacked** and select the `/extension` directory.

### Backend Setup
1. Navigate to the `/backend` directory.
2. Configure your Neon.tech PostgreSQL credentials in:
src/main/resources/application.properties
3. Run the application:
```bash
mvn spring-boot:run
```
### Data Flow

<p align="center">
  <img src="assets/CourseCompass_System_Overview.png" alt="CourseCompass Data Flow Diagram" width="800"/>
</p>

Trigger – User clicks the Analyze button injected into the Lou’s List table.

Request – The extension sends a GET request to the Spring Boot API.

Cache Check – The backend checks PostgreSQL for a recent cached course profile.

Scraping – On a cache miss, the server executes parallel Jsoup scraping tasks against Reddit and RateMyProfessors.

Processing – The backend aggregates results and computes a sentiment-based Vibe Score.

Response – The processed data is stored in the database and returned to the extension as JSON.

### License

This project is licensed under the MIT License.

### Contact

Aryan Thodupunuri
GitHub: https://github.com/AryanThodupunuri