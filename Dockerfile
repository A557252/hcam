
FROM adoptopenjdk/openjdk11:alpine-jre
RUN apk --no-cache add curl
MAINTAINER Sandeep-kumar.gupta@atos.net
EXPOSE 8080
HEALTHCHECK --start-period=30s --interval=5s CMD curl -f http://localhost:8080/hcam || exit 1
ARG JAR_FILE=target/hcam-0.0.1-SNAPSHOT.jar

# cd /opt/app
#WORKDIR /opt/app

# cp target/spring-boot-web.jar /opt/app/app.jar
COPY ${JAR_FILE} app.jar

# java -jar /opt/app/app.jar
ENTRYPOINT ["java","-jar","app.jar"]

#CMD  java -jar hcam.jar
#COPY build/libs/hcam.jar