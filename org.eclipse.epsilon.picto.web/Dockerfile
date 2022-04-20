# docker run --rm -i -t -v %cd%:/workspace --hostname=picto -p 8080:8080 --name=picto picto

#FROM openjdk:18-ea-11-jdk-alpine3.15
FROM openjdk:11-jre-slim-bullseye
WORKDIR /program/picto
COPY picto .
WORKDIR /program
COPY socialnetwork.ecore .
COPY socialnetwork.emf .
COPY socialnetwork.flexmi .
COPY picto.jar .
WORKDIR /workspace
RUN apt-get update -y && \
  apt-get install -y graphviz nocache && \
  apt-get -y autoremove
ENTRYPOINT ["java", "-jar", "/program/picto.jar"]
