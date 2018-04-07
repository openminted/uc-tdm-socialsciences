############################################################
#
# Dockerfile to build OMTD SSH Use-Case NER Docker Image
#
############################################################

FROM openjdk:8

MAINTAINER OpenMinTeD Team

# make sure docker image is running in en_US.UTF-8 locale
RUN set -ex \
      && DEBIAN_FRONTEND=noninteractive \
      && apt-get update \
      && apt-get install -y --no-install-recommends locales
RUN set -ex \
      && sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen \
      && dpkg-reconfigure --frontend=noninteractive locales \
      && update-locale LANG=en_US.UTF-8
ENV LANG en_US.UTF-8

WORKDIR /opt/ss-module-ner

COPY @docker.jarfile@ ss-module-ner-standalone.jar

ENTRYPOINT ["java","-Xmx750m","-Djava.awt.headless=true","-jar","ss-module-ner-standalone.jar"]
