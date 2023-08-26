#!/bin/sh
SCHEME="http"
if [ "$TLS_ENABLED" = "true" ] || [ "$TLS_ENABLED" = "1" ]; then
  SCHEME="https"
fi
curl -k $SCHEME://0.0.0.0:${SERVER_PORT:-8000}/identity/health_check
