#!/usr/bin/env bash
# FALLBACK ONLY: the backend now deploys to Scalingo (see SCALINGO.md), which
# auto-deploys on push to main. Cloud Run is a dormant fallback and its GitHub
# Action is disabled. This script still works for a manual Cloud Run deploy
# (remote Cloud Build).
set -euo pipefail

REGION="europe-west1"
SERVICE="transit-planner-backend"
STIB_API_KEY="${STIB_API_KEY:?Set STIB_API_KEY env var}"
CORS_ALLOWED_ORIGINS="${CORS_ALLOWED_ORIGINS:-http://localhost:5173}"

echo "Deploying backend to Cloud Run ($REGION)..."
echo "This takes ~10 min (remote Docker build + Java compilation)"

gcloud run deploy "$SERVICE" \
  --source . \
  --region "$REGION" \
  --allow-unauthenticated \
  --memory 512Mi \
  --cpu 1 \
  --min-instances 0 \
  --max-instances 1 \
  --port 8080 \
  --set-env-vars "^||^STIB_API_KEY=${STIB_API_KEY}||CORS_ALLOWED_ORIGINS=${CORS_ALLOWED_ORIGINS}"

echo ""
echo "Deployed. Verify:"
echo "  curl https://${SERVICE}-621870148637.europe-west1.run.app/api/departures"
