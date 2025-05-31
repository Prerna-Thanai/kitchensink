FROM maven:3.9.5-eclipse-temurin-21 AS BUILD

ENV HOME=/home/usr/app
RUN mkdir -p $HOME
WORKDIR $HOME/

ADD . $HOME

RUN --mount=type=cache,target=/root/.m2 mvn clean install


FROM eclipse-temurin:21-jre-alpine AS RUN
ARG UID=10001
WORKDIR /app
EXPOSE 8080
COPY --chown=1001:0  --from=BUILD  /home/usr/app/target/kitchensink.jar /app/kitchensink.jar
CMD "java" "-jar" "kitchensink.jar"