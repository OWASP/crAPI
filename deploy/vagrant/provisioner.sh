#!/usr/bin/env bash

# Copyright 2020 Traceable, Inc.
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



readonly MOUNT_DIR=/tmp/crapi

# Exit on error
set -e

# Add docker key and repository
apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 9DC858229FC7DD38854AE2D88D81803C0EBFCD88
echo "deb [arch=amd64] https://download.docker.com/linux/ubuntu xenial stable" | sudo tee /etc/apt/sources.list.d/docker.list

# Install and docker
apt update -q
apt upgrade -qy
apt install -qy docker-ce

# Install docker-compose
curl -sL https://github.com/docker/compose/releases/download/1.18.0-rc2/docker-compose-`uname -s`-`uname -m` -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose

# Build crAPI
"$MOUNT_DIR/deploy/docker/build-all.sh"

# Install crAPI
mkdir /opt/crapi

cp "$MOUNT_DIR/deploy/docker/docker-compose.yml" /opt/crapi \
    && sed -i /opt/crapi/docker-compose.yml \
        -e "s/version: '3.7'/version: '3.3'/" \
        -e "s/127.0.0.1:8888:80/80:80/" \
        -e "s/127.0.0.1:8025:8025/8025:8025/"
cp "$MOUNT_DIR/deploy/vagrant/crapi.service" /etc/systemd/system/ \
    && systemctl daemon-reload \
    && systemctl enable crapi.service

# Start crAPI
systemctl start crapi

# Cleanup
docker system prune -f
docker image prune -a -f
