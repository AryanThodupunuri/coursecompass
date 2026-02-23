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
  <a href="https://chromewebstore.google.com/detail/agmlnkmkjbjkkhjejoodckjgmafnnifm?utm_source=item-share-cb">
    <img src="https://img.shields.io/badge/Chrome_Web_Store-Install-blue?style=for-the-badge&logo=googlechrome" />
  </a>
</p>

---

## What this is
CourseCompass injects an "Analyze" button into Hoos' List/SIS course pages and returns a short, sourced summary for a professor/course (RateMyProfessors + Reddit + GitHub). It's a lightweight assistant to help students pick sections with better fit and workload visibility.

If you're like me and you don't want to open 15 tabs just to figure out if a section is a good idea, this puts the “should I take this?” signal directly where you already register.

## Quick links
- Chrome Web Store listing: https://chromewebstore.google.com/detail/agmlnkmkjbjkkhjejoodckjgmafnnifm?utm_source=item-share-cb
- Extension folder: `/extension`
- Backend folder: `/backend`

## Features
- Adds an **Analyze** button next to instructors on Hoos' List pages.
- Aggregates public signal from:
  - RateMyProfessors (GraphQL)
  - Reddit discussion threads
  - GitHub repos (nice for project-heavy classes)
- Returns a short summary + any numeric rating we can confidently extract.
- Server-side caching (Postgres) to avoid repeated scraping.

## Table of contents
- [Overview](#what-this-is)
- [Features](#features)
- [Architecture](#architecture)
- [Install & run locally](#install--run-locally)
- [API](#api)
- [Troubleshooting](#troubleshooting)
- [Project structure](#project-structure)
- [Contact & privacy](#contact--privacy)

---

## Architecture
- Client: Chrome extension (Manifest V3) — injects buttons and shows a tooltip with results.
- Server: Spring Boot backend — orchestrates scraping, aggregates results, caches them in PostgreSQL.

High-level request flow:
1. You click **Analyze** on Hoos' List.
2. The extension sends `{ prof, course }` to the backend.
3. Backend checks cache → scrapes on cache miss → stores result → returns JSON.

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

## API

### `GET /api/v1/analyze`

Query params:
- `prof` (required): professor name (usually `Last, First`)
- `course` (required): course id like `CS 3240`

Example:
```bash
curl "http://localhost:8080/api/v1/analyze?prof=Sherriff,%20Mark&course=CS%203240"
```

Response (shape):
- `professorName`: string
- `courseId`: string
- `avgRating`: number | null
- `sentimentSummary`: string
- `redditTopThreads`: string[]
- `githubTopRepos`: string

## Troubleshooting

### The Analyze button doesn't show up
- Make sure the extension is loaded from `extension/` and **reloaded** after changes.
- Confirm you’re on a supported page: `hooslist.virginia.edu` or `sis.virginia.edu`.

### Clicking Analyze shows an error
- Open DevTools → Console and look for a network error.
- Confirm the backend is reachable:
  - Local: `http://localhost:8080/health`
  - Deployed: `https://course-compass-api.onrender.com/health`

### Works locally but not on Render
- Check Render logs for DB connection errors.
- Ensure env vars are set (especially `DB_PASSWORD`).

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

Full policy: `PRIVACY_POLICY.md`

---

Licensed under the MIT License.
