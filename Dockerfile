FROM openjdk:8-alpine

ENV BACKEND_IP 127.0.0.1
ENV BACKEND_PORT 8083
ENV SERVER_IP 127.0.0.1
ENV SERVER_PORT 8084
  
RUN mkdir /explorviz
WORKDIR /explorviz
COPY build/libs/explorviz-discovery-agent.jar .
RUN mkdir META-INF
COPY build/resources/main/explorviz.properties META-INF/explorviz-custom.properties

COPY prod-env-updater.sh .
RUN chmod +x ./prod-env-updater.sh

CMD ./prod-env-updater.sh && java -cp explorviz-discovery-agent.jar:META-INF net.explorviz.discoveryagent.server.main.Main