# Usare un'immagine base con Java
FROM eclipse-temurin:17-jdk

# Impostare la working directory
WORKDIR /app

# Copiare il jar nella working directory
COPY build/libs/webgui-0.0.1-SNAPSHOT.jar  app.jar

# Esporre la porta su cui gira Spring Boot
EXPOSE 6767

# Comando per eseguire l'app
ENTRYPOINT ["java", "-jar", "app.jar"]

#./gradlew bootJar per generare il jar
# ## 2) docker build -t webgui:1.0 .
# docker tag webgui:1.0 lucacapelli/webgui:1.0
# docker push lucacapelli/webgui:1.0