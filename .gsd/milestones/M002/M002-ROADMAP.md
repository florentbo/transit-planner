# M002: Deploy to GCloud

**Vision:** Backend deployed and accessible at a public URL, serving real STIB departure data.

## Success Criteria

- Dockerfile builds a runnable Spring Boot image
- application.yml reads PORT env var with fallback to 8080
- Cloud Run service is live and responding with real STIB departure data
- DEPLOYMENT.md documents the full deployment process

## Slices

- [x] **S01: Cloud Run Deployment Infrastructure** `risk:low` `depends:[]`
  > After this: Backend is live on Cloud Run, returning real STIB departure data at a public URL
