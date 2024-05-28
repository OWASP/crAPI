#!/bin/sh

#
# Licensed under the Apache License, Version 2.0 (the “License”);
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an “AS IS” BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Get script directory
DIR="$( cd "$( dirname "$0" )" >/dev/null 2>&1 && pwd )"

# Load the data
echo "Check Django models"
python3 manage.py migrate

python3 manage.py check &&\
python3 manage.py health_check
if [ $? -ne 0 ]; then
  echo "Django database check failed. Exiting."
  exit 1
fi

echo "Seeding the database"
python3 manage.py seed_database
if [ $? -ne 0 ]; then
  echo "Django database seeding failed. Exiting."
  exit 1
fi

echo "Starting Django server"
if [ "$TLS_ENABLED" = "true" ] || [ "$TLS_ENABLED" = "1" ]; then
  echo "TLS is ENABLED"
  # if $TLS_CERTIFICATE and $TLS_KEY are not set, use the default ones
  if [ "$TLS_CERTIFICATE" = "" ]; then
    TLS_CERTIFICATE=$DIR/certs/server.crt
  fi
  if [ "$TLS_KEY" = "" ]; then
    TLS_KEY=$DIR/certs/server.key
  fi
  echo "TLS_CERTIFICATE: $TLS_CERTIFICATE"
  echo "TLS_KEY: $TLS_KEY"
  # python3 manage.py runserver_plus --cert-file $TLS_CERTIFICATE --key-file $TLS_KEY --noreload 0.0.0.0:${SERVER_PORT}
  gunicorn --workers=1 --threads=20  --timeout 60 --bind 0.0.0.0:${SERVER_PORT} --certfile $TLS_CERTIFICATE --keyfile $TLS_KEY --log-level=debug crapi_site.wsgi
else
  echo "TLS is DISABLED"
  # python3 manage.py runserver 0.0.0.0:${SERVER_PORT} --noreload
  gunicorn --workers=1 --threads=20  --timeout 60 --bind 0.0.0.0:${SERVER_PORT} --log-level=debug crapi_site.wsgi
fi
