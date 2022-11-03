#!/bin/sh
set -e

JWT_PRIVATE_KEY=$(openssl rsa -in /.keys/private.pem -outform der | openssl base64 -A)
JWT_PUBLIC_KEY=$(openssl rsa -in /.keys/private.pem -RSAPublicKey_out -outform DER | openssl base64 -A)

echo "Private Key: ${JWT_PRIVATE_KEY}"
echo "Public Key: ${JWT_PUBLIC_KEY}"

java -jar /app/identity-service-1.0-SNAPSHOT.jar

exec "$@"
