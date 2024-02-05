# syntax=docker/dockerfile:1

# Sample command to build this container image
# docker build -t bachupaul/repose-external:v1  --build-arg feeds_filter_version=1.7.0-SNAPSHOT --build-arg SCHEMA_VERSION=1.138.3-SNAPSHOT --build-arg saxon_lic="docker/saxon-license.lic" --build-arg repose_valve=external  -f Dockerfile ../
# docker build -t bachupaul/repose-external:v1  --build-arg feeds_filter_version=1.7.0-SNAPSHOT --build-arg SCHEMA_VERSION=1.138.3-SNAPSHOT --build-arg repose_valve=external  -f Dockerfile ../

# =============== Clone cloudfeeds-repose-filters repo to build container image in AWS EC2 Image Builder ===============
FROM bitnami/git:latest AS fetcher

# Pass custom values for any Below ARG variables from ECS Container Definition
ARG git_url=https://github.com/rackerlabs/cloudfeeds-repose-filters.git

# ARG git_branch="feature_repose_valve_container"
ARG git_branch="CF-3815-EC2-image-builder-for-repose"


ENV GIT_URL=${GIT_URL:-$git_url} \
    GIT_BRANCH=${GIT_BRANCH:-$git_branch}

RUN mkdir -p /home/rf && \
    cd /home && \
    git clone -b ${GIT_BRANCH} ${GIT_URL} rf


# =========================== Build Cloudfeeds repose filter artifacts ===========================

# FROM maven:3.8.6-openjdk-8-slim AS builder
FROM maven:3-eclipse-temurin-8-alpine AS builder

LABEL com.rackspace.cloudfeeds.image.maintainer="cloudfeeds-core@rackspace.com" \
      com.rackspace.cloudfeeds.image.description="Docker image for Cloudfeeds Repose Filters" \
      com.rackspace.cloudfeeds.image.name="repose" \
      com.rackspace.cloudfeeds.image.source="https://github.com/rackerlabs/cloudfeeds-repose-filters"

WORKDIR /cf-repose-filters

COPY --from=fetcher /home/rf/pom.xml                              ./pom.xml
COPY --from=fetcher /home/rf/feeds-filters/pom.xml                ./feeds-filters/pom.xml
COPY --from=fetcher /home/rf/json-xml-filter/pom.xml              ./json-xml-filter/pom.xml
# COPY functional-tests/pom.xml              ./functional-tests/pom.xml

RUN mvn  -B dependency:resolve
#RUN mvn  dependency:go-offline

#UsageSchema version
ARG SCHEMA_VERSION

#Repose image exposes /etc/repose as volume hence adding any artifact by wget of curl are getting removed,
#only way is to COPY or ADD, hance creating a multistage Dockerfile to curl and add the schema package

# If image = maven:3-eclipse-temurin-8-alpine
RUN apk add --update --no-cache curl tar && \
    curl https://artifacts.rackspace.net/artifactory/cloudfeeds-maven-local/com/rackspace/usage/usage-schema/${SCHEMA_VERSION}/usage-schema-${SCHEMA_VERSION}-schema.tar.gz  | tar xz


# If image = maven:3.8.6-openjdk-8-slim
# RUN apt-get update \
#     && apt-get -y install --no-install-recommends  curl tar \
#     && rm -rf /var/lib/apt/lists/* \
#     && curl https://artifacts.rackspace.net/artifactory/cloudfeeds-maven-local/com/rackspace/usage/usage-schema/${SCHEMA_VERSION}/usage-schema-${SCHEMA_VERSION}-schema.tar.gz  | tar xz


COPY --from=fetcher /home/rf/feeds-filters/src ./feeds-filters/src
COPY --from=fetcher /home/rf/json-xml-filter/src ./json-xml-filter/src
# COPY functional-tests/src ./functional-tests/src

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
ARG feeds_filter_version

ARG SCHEMA_VERSION

#Saxon License file 
ARG saxon_lic

#Repose valve type default value is custom filters (common configuration)
ARG repose_valve=common

ARG dumb_init_version=1.2.5

ENV FEEDS_FILTER_VERSION=${FEEDS_FILTER_VERSION:-$feeds_filter_version}


LABEL com.rackspace.cloudfeeds.image.maintainer="cloudfeeds-core@rackspace.com" \
      com.rackspace.cloudfeeds.image.description="Docker image for Cloudfeeds Repose Filters" \
      com.rackspace.cloudfeeds.image.name="repose" \
      com.rackspace.cloudfeeds.image.version="${FEEDS_FILTER_VERSION}" \
      com.rackspace.cloudfeeds.image.source="https://github.com/rackerlabs/cloudfeeds-repose-filters"

ENV SAXON_HOME=/etc/saxon \
    DESTINATION_PORT=8080 \
    REPOSE_PORT=9090 \
    APP_ROOT=/etc/repose \
    # OpenTelemetry
    AWS_OTEL_JAVA_AGENT_VERSION="v1.29.0" \
    OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317 \
    OTEL_TRACES_EXPORTER=otlp \
    OTEL_METRICS_EXPORTER=otlp
# JAVA_OPTS="-Dcom.sun.management.jmxremote.port=1299 -Dcom.sun.management.jmxremote.password.file=/etc/repose/jmxremote.password -Dcom.sun.management.jmxremote.access.file=/etc/repose/jmxremote.access -Dcom.sun.management.jmxremote.ssl=false -Xms2048m -Xmx2048m"

USER root

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
      curl -L --output /tmp/aws-opentelemetry-agent.jar https://github.com/aws-observability/aws-otel-java-instrumentation/releases/download/${AWS_OTEL_JAVA_AGENT_VERSION}/aws-opentelemetry-agent.jar
      chown repose /tmp/aws-opentelemetry-agent.jar
EOF

# with `repose` user getting error as `cannot able to create '/var/log/repose/repose.log' file`
# USER repose

COPY --chown=repose:repose --from=fetcher /home/rf/docker/configuration/common /home/rf/docker/configuration/${repose_valve} /home/rf/docker/configuration/files/jmx/ ${APP_ROOT}/
COPY --chown=repose:repose --from=fetcher /home/rf/docker/configuration/files/saxon.sh /home/rf/docker/configuration/files/saxon.csh /etc/profile.d/
COPY --chown=repose:repose --from=fetcher /home/rf/docker/configuration/files/repose-logrotate /etc/logrotate.d/repose
# COPY --chown=repose:repose ${saxon_lic} ${SAXON_HOME}/saxon-license.lic
COPY --chown=repose:repose --from=builder /cf-repose-filters/usage-schema-${SCHEMA_VERSION} ${APP_ROOT}/usage-schema/
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
CMD ["java", "-javaagent:/tmp/aws-opentelemetry-agent.jar", "-jar", "/usr/share/repose/repose.jar", "-c", "/etc/repose"]
