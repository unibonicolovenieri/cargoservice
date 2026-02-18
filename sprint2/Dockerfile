## ----------------------------------------------------------
## Dockerfile for unibo.basicrobot24
## ----------------------------------------------------------

FROM eclipse-temurin:17-jdk
EXPOSE 8020
## ADD extracts the tar
RUN apt-get update -y && \
    apt-get install -y python3 && \
    ln -s /usr/bin/python3 /usr/bin/python && \
    apt-get clean
COPY ./build/libs/sprint2-1.0.jar sprint2:1.0.jar
COPY ./*.pl ./
COPY ./*.json ./
COPY ./*.bin ./
COPY ./*.txt ./
COPY ./*.py ./

## RUN apt-get update -y
## RUN apt-get install -y wiringpi
## RUN sudo apt-get install -y python

CMD ["java", "-jar", "sprint2:1.0.jar"]

## 0) docker rmi sprin1:1.0
## 1) gradlew distTar
## 2) docker build -t sprin1:1.0 .
## OCCORRE APRIRE UDP PER PERMETTERE COAP
## docker run -it --rm --name basicrobot24 -p8020:8020/tcp -p8020:8020/udp  --privileged basicrobot24:3.0  /bin/bash
## MODIFICA DEL FILE DI CONFIGURAZIONE
# docker cp basicrobotConfig.json 89193dba02a2:/basicrobot24-1.0/bin/basicrobotConfig.json
# docker cp stepTimeConfig.json cab3aa29f8eb:/basicrobot24-1.0/bin/stepTimeConfig.json

# docker tag sprin1:1.0 lucacapelli/sprin1:1.0

#type docker_password.txt | docker login --username natbodocker --password-stdin
#docker login --username natbodocker --password xyz
# docker push lucacapelli/sprin1:1.0


# docker-compose -f sprint1.yaml  up
## ----------------------------------------------------------
## Dockerfile for arch1
## ----------------------------------------------------------