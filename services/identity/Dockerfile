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


# Java Maven Build
FROM gradle:8.1-jdk17-focal AS gradlebuild
WORKDIR /app
COPY *.gradle.kts ./
COPY gradle.properties ./
# Only download dependencies
# Eat the expected build failure since no source code has been copied yet
RUN gradle clean build --no-daemon > /dev/null 2>&1 || true
COPY src src
RUN gradle build
RUN gradle bootJar

# Main Image
FROM openjdk:17.0-jdk-slim
# #Java
RUN apt-get -y update && apt-get -y install curl && apt-get -y clean
# RUN apt-get -y update && apt-get -y install wget curl unzip && apt-get -y clean
# RUN wget https://www.yourkit.com/download/docker/YourKit-JavaProfiler-2023.9-docker.zip -P /tmp/ && \
#   unzip /tmp/YourKit-JavaProfiler-2023.9-docker.zip -d /usr/local && \
#   rm /tmp/YourKit-JavaProfiler-2023.9-docker.zip
RUN mkdir /app
COPY --from=gradlebuild /app/build/libs/identity-service-1.0-SNAPSHOT.jar /app/identity-service-1.0-SNAPSHOT.jar

ARG SERVER_PORT
EXPOSE ${SERVER_PORT}
EXPOSE 10001

ENV JAVA_TOOL_OPTIONS "-Xmx128m"

COPY jwks.json default_jwks.json
COPY entrypoint.sh /entrypoint.sh
COPY health.sh /app/health.sh
CMD [ "/entrypoint.sh"]
