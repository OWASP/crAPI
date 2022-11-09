#!/bin/sh
set -e

JWKS=$(openssl base64 -in /.keys/jwks.json -A)
java -jar /app/identity-service-1.0-SNAPSHOT.jar --app.jwksJson=$JWKS

exec "$@"
