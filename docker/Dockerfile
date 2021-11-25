#Repose image exposes /etc/repose as volume hence adding any artifact by wget of curl are getting removed,
#only way is to COPY or ADD, hance creating a multistage Dockerfile to curl and add the schema package

FROM alpine:3.13 as feeds_artifacts

#UsageSchema version
ARG schema_version
#Custom Feeds Filters Version 
ARG feeds_filters_version

ENV SCHEMA_VERSION=${schema_version} \
    FEEDS_FILTER_VERSION=${feeds_filters_version}

RUN apk --no-cache add curl tar \
    && curl https://artifacts.rackspace.net/artifactory/cloudfeeds-maven-local/com/rackspace/usage/usage-schema/${schema_version}/usage-schema-${schema_version}-schema.tar.gz ${APP_ROOT} | tar xz \
    && curl -o feeds-filters-${FEEDS_FILTER_VERSION}.ear https://artifacts.rackspace.net/artifactory/cloudfeeds-maven-local/com/rackspace/feeds/repose/feeds-filters/${FEEDS_FILTER_VERSION}/feeds-filters-${FEEDS_FILTER_VERSION}.ear


FROM repose-docker-local.artifacts.rackspace.net/ubuntu/reposeimage:latest

USER root
#Saxon License file 
ARG saxon_lic
#Repose valve type default value is custom filters (common configuration)
ARG repose_valve=common

ENV SAXON_HOME=/etc/saxon \
    DESTINATION_PORT=8080 \
    http_proxy= \
    APP_ROOT=/etc/repose \
    JAVA_OPTS=
#"-Dcom.sun.management.jmxremote.port=1299 -Dcom.sun.management.jmxremote.password.file=/etc/repose/jmxremote.password -Dcom.sun.management.jmxremote.access.file=/etc/repose/jmxremote.access -Dcom.sun.management.jmxremote.ssl=false -Xms2048m -Xmx2048m" 

RUN mkdir -p ${SAXON_HOME} /var/log/repose/
#Inherited from Repose image
WORKDIR ${APP_ROOT}
#Remove existing configuration
RUN rm -r ${APP_ROOT}/*

COPY configuration/common configuration/${repose_valve} configuration/files/jmx/ ${APP_ROOT}/
COPY configuration/files/saxon.sh configuration/files/saxon.csh /etc/profile.d/
COPY configuration/files/repose-logrotate /etc/logrotate.d/repose
COPY ${saxon_lic} ${SAXON_HOME}/saxon-license.lic
COPY --from=feeds_artifacts /usage-schema* ${APP_ROOT}/usage-schema
COPY --from=feeds_artifacts feeds-filters-*.ear /usr/share/repose/filters/

RUN chmod +x /usr/share/repose/filters/feeds-filters-*.ear

EXPOSE 9090 
# Start Repose.
CMD sed -i -e 's/port="8080"/port=\"'${DESTINATION_PORT}'\"/' system-model.cfg.xml && java $JAVA_OPTS -jar /usr/share/repose/repose.jar -c /etc/repose && tail -f /var/log/repose/repose.log
