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
EXPOSE 8080

ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.7.3/wait /wait
RUN chmod +x /wait

CMD /wait && java -jar /app/user-microservices-1.0-SNAPSHOT.jar
