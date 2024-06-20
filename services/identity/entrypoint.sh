#!/bin/sh
set -e

if [ -f /keys/jwks.json ]; then
  JWKS=$(openssl base64 -in /keys/jwks.json -A)
else
  echo "Loading default JWKS file."
  JWKS=$(openssl base64 -in /default_jwks.json -A)
fi
java -jar /app/identity-service-1.0-SNAPSHOT.jar --app.jwksJson=$JWKS

exec "$@"
