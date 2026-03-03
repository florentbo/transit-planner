#!/usr/bin/env bash
set -euo pipefail

REGION="europe-west1"
SERVICE="transit-planner-backend"
PROJECT="transport-login"
REPO="cloud-run-source-deploy"
IMAGE="europe-west1-docker.pkg.dev/${PROJECT}/${REPO}/${SERVICE}"
STIB_API_KEY="${STIB_API_KEY:?Set STIB_API_KEY env var}"
CORS_ALLOWED_ORIGINS="${CORS_ALLOWED_ORIGINS:-http://localhost:5173}"

echo "Building Docker image locally..."
docker build -t "$IMAGE" .

echo "Pushing image to Artifact Registry..."
docker push "$IMAGE"

echo "Deploying to Cloud Run ($REGION)..."
gcloud run deploy "$SERVICE" \
  --image "$IMAGE" \
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
