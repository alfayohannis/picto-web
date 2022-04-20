# Picto Web

## Introduction

[This web app](https://github.com/alfayohannis/picto-web) should be the refactor version of [Picto](https://www.eclipse.org/epsilon/doc/picto/) so that its core becomes independent of Eclipse. The app should be able to read a .picto file and display the generated tree and views. It should be able to monitor the directory of the .picto file and when files in it change, it should ask the front-end to auto-refresh. The app is packaged in a Docker container (see the [Epsilon Playground](https://www.eclipse.org/epsilon/live/) and its [dockerized version](https://github.com/epsilonlabs/playground-docker) as examples).

## Docker
The docker image of Picto Web can be temporarily found here: [https://hub.docker.com/r/alfayohannisyorkacuk/picto-web](https://hub.docker.com/r/alfayohannisyorkacuk/picto-web). Execute the following command to pull it from https://hub.docker.com:
```
docker pull alfayohannisyorkacuk/picto-web
```
If your operating system is Windows, run the command below to run the Picto Web server. The variable `%cd%` represents your current working directory in Windows. Replace it with `$PWD`, if you use Linux as your operating system, or replace it with any directory path if you want to set the directory as the working directory of Picto Web.
```
docker run --rm -i -t -v %cd%:/workspace --hostname=picto -p 8080:8080 --name=picto alfayohannisyorkacuk/picto-web
```
Browse http://localhost:8080 to load Picto Web app.
