#!/bin/sh
envsubst '${GO_SERVICE} ${JAVA_SERVICE} ${PYTHON_SERVICE}' < /etc/nginx/conf.d/default.conf.template > /etc/nginx/conf.d/default.conf
openresty
exec "$@"