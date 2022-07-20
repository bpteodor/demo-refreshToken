FROM openjdk:17-slim

LABEL maintainer="Teo<teodor.bran@ic-consult.com>"

ARG CATALINA_HOME=$CATALINA_HOME

WORKDIR /srv/app/

RUN apt-get update \
 && apt-get install --assume-yes libfreetype6 \
 && apt-get install --assume-yes libfontconfig1 \
 && rm -rf /var/lib/apt/lists/*

RUN adduser --home /srv/app --uid 1000 --gecos "" --ingroup root --system app

COPY --chown=1000:0 target/app-jar-with-dependencies.jar /srv/app/drt.jar

USER 1000

ENTRYPOINT ["java", "-jar", "/srv/app/drt.jar"]