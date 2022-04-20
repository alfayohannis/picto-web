# run the following command to use this docker container later after installing
# docker run --rm -d -i -t -v %cd%:/fairml --hostname=fairml -p 8888:8888 --name=fairml fairml -w demo.flexmi
# docker run --rm -d -i -t -v %cd%:/fairml --hostname=fairml -p 8888:8888 --name=fairml fairml demo.flexmi
# docker run --rm -d -i -t -v %cd%:/fairml --hostname=fairml -p 8888:8888 --name=fairml fairml -j demo.ipynb
# docker exec fairml bash -c "jupyter notebook list"
# docker exec -i fairml bash -c "python3 -m notebook.auth password"
# jupyter notebook dcatmo.ipynb --port=8888 --no-browser --ip='*' --allow-root --NotebookApp.password_required=False --NotebookApp.allow_remote_access=True --NotebookApp.token='' --NotebookApp.password=''
# FROM openjdk:11-jdk-slim-bullseye

# STAGE 01: Do all the Java stuff to build only the fairml
# FROM openjdk:11-jdk-alpine AS stage01
FROM debian:bullseye-slim AS Stage01

WORKDIR /picto

RUN apt-get update -y && \
  apt-get install -y openjdk-11-jre-headless nocache && \
  apt-get install -y git nocache && \
  apt-get install -y maven nocache && \
  https://github.com/alfayohannis/picto-web.git && \
  cd /picto && \
  apt-get remove -y git nocache && \
  apt-get autoremove -y

WORKDIR /picto/picto-web/org.eclipse.epsilon.picto.web

RUN ./mvnw package && \
  cp -v picto.jar /picto && \
  apt-get remove -y maven nocache && \
  apt-get autoremove -y && \
  rm -rf /picto/picto-web && \
  rm -rf /root/.m2/repository 

# STAGE 02: copy the fairml in stage 2 and install all python dependencies
# FROM debian:bullseye-slim
FROM openjdk:11-jre-slim-bullseye

WORKDIR /picto

COPY --from=Stage01 /picto/fairml.jar /picto/

RUN apt-get update -y && \
  apt-get autoremove -y

WORKDIR /fairml

ENTRYPOINT ["java", "-cp", "/picto/picto.jar", "org.eclipse.epsilon.picto.web"]
#ENTRYPOINT ["java", "-jar", "picto.jar"]
