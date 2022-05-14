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
FROM maven:3.6-jdk-11 AS javabuild
RUN mkdir /app
COPY ./pom.xml /app/pom.xml
RUN mvn -f /app/pom.xml dependency:go-offline -B 
COPY ./src /app/src
RUN mvn -f /app/pom.xml clean package -DskipTests

# Main Image
FROM openjdk:11-jre-slim-buster

#Java
RUN apt-get -y update && apt-get -y install curl && apt-get -y clean
RUN mkdir /app
COPY --from=javabuild /app/target/user-microservices-1.0-SNAPSHOT.jar /app/user-microservices-1.0-SNAPSHOT.jar

ARG SERVER_PORT 
EXPOSE ${SERVER_PORT}

ENV JAVA_TOOL_OPTIONS "-Xmx128m"

CMD java -jar /app/user-microservices-1.0-SNAPSHOT.jar
