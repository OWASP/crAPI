#!/bin/sh
set -e

JWT_PRIVATE_KEY=$(openssl pkcs8 -topk8 -in /.keys/private.pem -outform der -nocrypt | openssl base64 -A)
JWT_PUBLIC_KEY=$(openssl pkey -in /.keys/private.pem -pubout -outform der | openssl base64 -A)

java -jar /app/identity-service-1.0-SNAPSHOT.jar --app.jwtPrivateKey=$JWT_PRIVATE_KEY --app.jwtPublicKey=$JWT_PUBLIC_KEY

exec "$@"
