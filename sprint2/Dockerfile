## ----------------------------------------------------------
## Dockerfile for unibo.basicrobot24
## ----------------------------------------------------------

FROM eclipse-temurin:17.0.5_8-jre-focal as builder
## EXPOSE 8020
## ADD extracts the tar
ADD ./build/distributions/sprint2-1.0.tar /
WORKDIR /sprint2-1.0/bin
COPY ./*.pl ./
COPY ./*.json ./
COPY ./*.bin ./
COPY ./*.txt ./
COPY ./*.py ./

RUN apt-get update -y
RUN apt-get install -y wiringpi
RUN apt-get install -y python3


RUN apt-get install -y python3-pip 
RUN apt-get clean

# Installa RPi.GPIO
RUN pip install RPi.GPIO

CMD ["bash", "sprint2"]

## 0) docker rmi sprint2:1.0
## 1) gradlew distTar
## 2) docker build -t sprint2:1.0 .
# docker cp basicrobotConfig.json 89193dba02a2:/basicrobot24-1.0/bin/basicrobotConfig.json
# docker cp stepTimeConfig.json cab3aa29f8eb:/basicrobot24-1.0/bin/stepTimeConfig.json

# docker tag sprint2:1.0 lucacapelli/sprint2:1.0

# docker push lucacapelli/sprint2:1.0


# docker-compose -f sprint2.yaml  up
## ----------------------------------------------------------
## Dockerfile for arch1
## ----------------------------------------------------------
