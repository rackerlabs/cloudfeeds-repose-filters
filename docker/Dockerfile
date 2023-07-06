# syntax=docker/dockerfile:1

# Sample command to build this container image
# docker build -t cf-repose-filters:v1  --build-arg FEEDS_FILTER_VERSION=1.7.0-SNAPSHOT --build-arg SCHEMA_VERSION=1.138.3-SNAPSHOT --build-arg saxon_lic="docker/saxon-license.lic" --build-arg repose_valve=external  -f Dockerfile ../

# =========================== Build Cloudfeeds feeds repose filter artifacts ===========================

# FROM maven:3.8.6-openjdk-8-slim AS builder
FROM maven:3-eclipse-temurin-8-alpine AS builder

LABEL Maintainer="cloudfeeds-core@rackspace.com" \
    description="Docker image for Cloudfeeds Repose Filters" \
    NAME="cloudfeeds-repose-filters"

WORKDIR /cf-repose-filters

COPY pom.xml                              ./pom.xml
COPY feeds-filters/pom.xml                ./feeds-filters/pom.xml
COPY json-xml-filter/pom.xml              ./json-xml-filter/pom.xml

RUN mvn  -B dependency:resolve
#RUN mvn  dependency:go-offline

#UsageSchema version
ARG SCHEMA_VERSION

#Repose image exposes /etc/repose as volume hence adding any artifact by wget of curl are getting removed,
#only way is to COPY or ADD, hance creating a multistage Dockerfile to curl and add the schema package

# If image = maven:3-eclipse-temurin-8-alpine
# RUN apk add --update --no-cache curl tar
# RUN tar -xz /cf-repose-filters/usage-schema-1.138.4-SNAPSHOT-schema.tar.gz


# If image = maven:3.8.6-openjdk-8-slim
# RUN apt-get update \
#     && apt-get -y install --no-install-recommends  curl tar \
#     && rm -rf /var/lib/apt/lists/* \
#     && curl https://artifacts.rackspace.net/artifactory/cloudfeeds-maven-local/com/rackspace/usage/usage-schema/${SCHEMA_VERSION}/usage-schema-${SCHEMA_VERSION}-schema.tar.gz  | tar xz


COPY feeds-filters/src ./feeds-filters/src
COPY json-xml-filter/src ./json-xml-filter/src

#RUN mvn -B package -DskipTests
RUN mvn  -B package


# ================================== Lint java code ==================================
# FROM openjdk:8-jre-alpine AS lint
# RUN wget https://github.com/checkstyle/checkstyle/releases/download/checkstyle-8.15-all.jar
# WORKDIR /cf-repose-filters
# COPY checks.xml .
# COPY feeds-filters/src ./feeds-filters/src
# COPY json-xml-filter/src ./json-xml-filter/src
# RUN java -jar checkstyle-8.15-all.jar -c checks.xml ./feeds-filters/src
# RUN java -jar checkstyle-8.15-all.jar -c checks.xml ./json-xml-filter/src


# ======================== Build Cloudfeeds Repose Filters container image ========================

# FROM repose-docker-local.artifacts.rackspace.net/ubuntu/reposeimage:9.1.0.1

FROM rackerlabs/repose:9.1.0.0

#Custom Feeds Filters Version
ARG FEEDS_FILTER_VERSION

ARG SCHEMA_VERSION

#Saxon License file 
ARG saxon_lic

#Repose valve type default value is custom filters (common configuration)
ARG repose_valve=common

ARG dumb_init_version=1.2.5

LABEL Maintainer="cloudfeeds-core@rackspace.com" \
    description="Docker image for Cloudfeeds Repose Filters" \
    NAME="cloudfeeds-repose-filters" \
    Version="${FEEDS_FILTER_VERSION}"

ENV SAXON_HOME=/etc/saxon \
    DESTINATION_PORT=8080 \
    REPOSE_PORT=9090 \
    APP_ROOT=/etc/repose
# JAVA_OPTS="-Dcom.sun.management.jmxremote.port=1299 -Dcom.sun.management.jmxremote.password.file=/etc/repose/jmxremote.password -Dcom.sun.management.jmxremote.access.file=/etc/repose/jmxremote.access -Dcom.sun.management.jmxremote.ssl=false -Xms2048m -Xmx2048m"

USER root

# RUN mkdir -p ${SAXON_HOME} /var/log/repose/

WORKDIR ${APP_ROOT}

# use existing non privileged user `repose`
RUN <<EOF
      apt-get update
      apt-get -y install --no-install-recommends  curl
      rm -rf /var/lib/apt/lists/*

      curl -L -O  https://github.com/Yelp/dumb-init/releases/download/v${dumb_init_version}/dumb-init_${dumb_init_version}_amd64.deb
      dpkg -i dumb-init_${dumb_init_version}_amd64.deb
      chown repose /usr/bin/dumb-init
      rm -f dumb-init_${dumb_init_version}_amd64.deb
EOF

# with `repose` user getting error as `cannot able to create '/var/log/repose/repose.log' file`
# USER repose

COPY --chown=repose:repose docker/configuration/common docker/configuration/${repose_valve} docker/configuration/files/jmx/ ${APP_ROOT}/
COPY --chown=repose:repose docker/configuration/files/saxon.sh docker/configuration/files/saxon.csh /etc/profile.d/
COPY --chown=repose:repose docker/configuration/files/repose-logrotate /etc/logrotate.d/repose
COPY --chown=repose:repose ${saxon_lic} ${SAXON_HOME}/saxon-license.lic
COPY --chown=repose:repose usage-schema-1.138.4-SNAPSHOT ${APP_ROOT}/usage-schema/
COPY --chown=repose:repose --from=builder /cf-repose-filters/feeds-filters/target/feeds-filters-${FEEDS_FILTER_VERSION}.ear /usr/share/repose/filters/
# Check if we need to copy 'json-xml-filter/target/json-xml-filter-${FEEDS_FILTER_VERSION}.jar'

# USER repose

RUN chmod 555 /usr/share/repose/filters/feeds-filters-${FEEDS_FILTER_VERSION}.ear


EXPOSE ${REPOSE_PORT}

# Perform health check
HEALTHCHECK --interval=30s --timeout=30s --start-period=5s --retries=3 \
            CMD curl -L --fail --silent --show-error --connect-timeout 3 --max-time 3 http://localhost:${REPOSE_PORT}/buildinfo || exit 1

# Start Repose.
# CMD sed -i -e 's/port="8080"/port=\"'${DESTINATION_PORT}'\"/' system-model.cfg.xml && java $JAVA_OPTS -jar /usr/share/repose/repose.jar -c /etc/repose && tail -f /var/log/repose/repose.log
#CMD java $JAVA_OPTS -jar /usr/share/repose/repose.jar -c /etc/repose && tail -f /var/log/repose/repose.log

#Start Repose
# https://one.rackspace.com/display/cloudfeeds/Cloud+Feeds+-+HOWTO+check+the+status+of+repose
# /usr/bin/java -javaagent:/etc/newrelic/newrelic.jar -Dnewrelic.config.file=/etc/newrelic/newrelic.yml -Dcom.sun.management.jmxremote=1299 -Dcom.sun.management.jmxremote.password.file=/etc/repose/jmxremote.password -Dcom.sun.management.jmxremote.access.file=/etc/repose/jmxremote.access -Dcom.sun.management.jmxremote.ssl=false -Xms2048m -Xmx2048m -XX:MaxPermSize=512m -jar /usr/share/lib/repose/repose-valve.jar START -s 8188 -p 9090 -c /etc/repose

ENTRYPOINT ["/usr/bin/dumb-init", "--"]
# CMD ["java", "${JAVA_OPTS}", "-jar", "/usr/share/repose/repose.jar", "-Dcom.sun.management.jmxremote", "1299", "-Dcom.sun.management.jmxremote.password.file", "/etc/repose/jmxremote.password", "-Dcom.sun.management.jmxremote.access.file", "/etc/repose/jmxremote.access", "-Dcom.sun.management.jmxremote.ssl", "false", "-c", "/etc/repose", "&&", "tail", "-f", "/var/log/repose/repose.log"]
# CMD ["java", "${JAVA_OPTS}", "-jar", "/usr/share/repose/repose.jar", "-c", "/etc/repose"]
CMD ["java", "-jar", "/usr/share/repose/repose.jar", "-c", "/etc/repose"]
