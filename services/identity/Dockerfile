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
FROM gradle:7.3.3-jdk11 AS gradlebuild
WORKDIR /app
COPY *.gradle.kts ./
# Only download dependencies
# Eat the expected build failure since no source code has been copied yet
RUN gradle clean build --no-daemon > /dev/null 2>&1 || true
COPY src src
RUN gradle build
RUN gradle bootJar

# Main Image
FROM openjdk:11.0.15-jre-slim-buster

#Java
RUN apt-get -y update && apt-get -y install curl && apt-get -y clean
RUN mkdir /app
COPY --from=gradlebuild /app/build/libs/identity-service-1.0-SNAPSHOT.jar /app/identity-service-1.0-SNAPSHOT.jar

ARG SERVER_PORT 
EXPOSE ${SERVER_PORT}

ENV JAVA_TOOL_OPTIONS "-Xmx128m"

COPY jwks.json default_jwks.json
COPY entrypoint.sh /entrypoint.sh
COPY health.sh /app/health.sh
CMD [ "/entrypoint.sh"]
