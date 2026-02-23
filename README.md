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
  [![Chrome Web Store](https://img.shields.io/badge/Chrome_Web_Store-View-blue?style=for-the-badge&logo=googlechrome)](https://chromewebstore.google.com/detail/agmlnkmkjbjkkhjejoodckjgmafnnifm?utm_source=item-share-cb)
</p>

---

## What this is
CourseCompass injects an "Analyze" button into Hoos' List/SIS course pages and returns a short, sourced summary for a professor/course (RateMyProfessors + Reddit + GitHub). It's a lightweight assistant to help students pick sections with better fit and workload visibility.

## Quick links
- Chrome Web Store listing: https://chromewebstore.google.com/detail/agmlnkmkjbjkkhjejoodckjgmafnnifm?utm_source=item-share-cb
- Extension folder: `/extension`
- Backend folder: `/backend`

## Table of contents
- [Overview](#what-this-is)
- [Architecture](#architecture)
- [Install & run locally](#install--run-locally)
- [Project structure](#project-structure)
- [Contact & privacy](#contact--privacy)

---

## Architecture
- Client: Chrome extension (Manifest V3) — injects buttons and shows a tooltip with results.
- Server: Spring Boot backend — orchestrates scraping, aggregates results, caches them in PostgreSQL.

## Data flow

<p align="center">
  <img src="assets/CourseCompass_System_Overview.png" alt="CourseCompass Data Flow Diagram" width="800" />
</p>

## Install & run locally

Chrome extension:
1. Open `chrome://extensions` in Chrome.
2. Enable **Developer mode**.
3. Click **Load unpacked** and choose the `extension/` directory.

Backend (local):
1. Edit `backend/src/main/resources/application.properties` or set the `DB_*` environment variables.
2. From repo root:
```bash
cd backend
mvn spring-boot:run
```
3. Health: `http://localhost:8080/health`

When testing the extension locally, change `API_BASE` in `extension/content.js` to `http://localhost:8080/api/v1/analyze` and reload the extension.

## Project structure
```
/extension  # chrome extension (manifest + content script)
/backend    # Spring Boot API (controllers, services, model)
Dockerfile
render.yaml
```

## Contact & privacy
Aryan Thodupunuri — https://github.com/AryanThodupunuri

Privacy: this extension reads professor names and course identifiers from Hoos' List pages and sends them to the CourseCompass backend to compute a short summary. No personal identifiers or browsing history are collected.

---

Licensed under the MIT License.
