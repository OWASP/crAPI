rm -f server.p12
rm -f server.keystore
openssl pkcs12 -export -in server.crt -inkey server.key -out server.p12 -name identity -passout pass:passw0rd
keytool -importkeystore -deststorepass passw0rd -destkeypass passw0rd -destkeystore server.keystore -srckeystore server.p12 -srcstoretype PKCS12 -srcstorepass passw0rd -alias identity
