# M002: Deploy to GCloud

**Gathered:** 2026-03-03
**Status:** Complete

## Project Description

Deploy the Transit Planner backend to Google Cloud Run so the app is accessible outside localhost.

## Why This Milestone

The app is only usable on localhost — deploying to Cloud Run makes it accessible for real commute use.

## User-Visible Outcome

### When this milestone is complete, the user can:

- Access the backend API at a public Cloud Run URL
- Get real-time STIB departure data from the deployed service

### Entry point / environment

- Entry point: https://transit-planner-backend-621870148637.europe-west1.run.app/api/departures
- Environment: Google Cloud Run (europe-west1)
- Live dependencies involved: STIB OpenDataSoft API (STIB_API_KEY as env var on Cloud Run)

## Scope

### In Scope

- Dockerfile (multi-stage build)
- .dockerignore
- DEPLOYMENT.md
- application.yml PORT env var support

### Out of Scope / Non-Goals

- Frontend deployment (Netlify or similar)
- CI/CD pipeline
- Custom domain
