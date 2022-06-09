### HOW TO BUILD AND PUSH ####
# docker image build . -t picto
# docker tag picto-web:latest alfayohannisyorkacuk/picto-web:latest
# docker push alfayohannisyorkacuk/picto-web:latest

### HOW TO RUN ON WINDOWS ###
# docker run --rm -i -t -v %cd%:/workspace --hostname=picto -p 8080:8080 --name=picto picto
# docker run --rm -i -t -v %cd%:/workspace --hostname=picto -p 8080:8080 --name=picto alfayohannisyorkacuk/picto-web

### HOW TO RUN ON LINUX ###
# docker run --rm -i -t -v $PWD:/workspace --hostname=picto -p 8080:8080 --name=picto picto
# docker run --rm -i -t -v $PWD:/workspace --hostname=picto -p 8080:8080 --name=picto alfayohannisyorkacuk/picto-web

#FROM openjdk:18-ea-11-jdk-alpine3.15
FROM openjdk:11-jre-slim-bullseye
#WORKDIR /program/picto
#COPY picto .
WORKDIR /program
#COPY socialnetwork.ecore .
#COPY socialnetwork.emf .
#COPY socialnetwork.flexmi .
COPY picto.jar .
#WORKDIR /workspace
RUN apt-get update -y && \
  apt-get install -y graphviz nocache && \
  apt-get -y autoremove
ENTRYPOINT ["java", "-jar", "/program/picto.jar"]

