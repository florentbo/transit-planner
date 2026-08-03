#!/usr/bin/env bash
# Manual deploy to Cloud Run, the primary backend host (see DEPLOYMENT.md).
# Normal deploys go through the GitHub Action on push to main; use this script to
# recreate the service from scratch or to deploy without CI. Builds remotely on
# Cloud Build, so nothing compiles locally.
#
# Serving URL is https://transport-back.bonamis.be (Cloud Run domain mapping).
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
