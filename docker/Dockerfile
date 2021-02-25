#As repose image exposes /etc/repose as volume hence adding any artifact by wget of curl are getting removed,
#only way is to COPY or ADD, hance creating a multistage Dockerfile to curl and add the schema package

FROM alpine:3.13 as schema

#UsageSchema version
ARG schema_version
ENV SCHEMA_VERSION=${schema_version}
RUN apk --no-cache add curl tar \
    && curl https://maven.research.rackspacecloud.com/content/repositories/releases/com/rackspace/usage/usage-schema/${SCHEMA_VERSION}/usage-schema-${SCHEMA_VERSION}-schema.tar.gz ${APP_ROOT} | tar xz 


FROM repose-docker-local.artifacts.rackspace.net/ubuntu/reposeimage:latest

USER root

ENV SAXON_HOME=/etc/saxon \
    http_proxy= \
    APP_ROOT=/etc/repose \
    JAVA_OPTS="-Dcom.sun.management.jmxremote.port=1299 -Dcom.sun.management.jmxremote.password.file=/etc/repose/jmxremote.password -Dcom.sun.management.jmxremote.access.file=/etc/repose/jmxremote.access -Dcom.sun.management.jmxremote.ssl=false -Xms2048m -Xmx2048m" 

RUN mkdir -p ${SAXON_HOME} /var/log/repose/
#Inherited from Repose image
WORKDIR ${APP_ROOT}
#Remove existing configuration
RUN rm -r ${APP_ROOT}/*

COPY ./configuration/common ./files/jmx/ ${APP_ROOT}/
COPY ./files/saxon.sh ./files/saxon.csh /etc/profile.d/
COPY ./files/repose-logrotate /etc/logrotate.d/repose
COPY ./files/saxon/repose01us-stage-saxon-license.lic ${SAXON_HOME}/saxon-license.lic
COPY --from=schema /usage-schema* ${APP_ROOT}/usage-schema

RUN ls -ltr
#COPY ./docker/usage-schema /etc/repose/usage-schema
EXPOSE 9090 
# Start Repose.
CMD java $JAVA_OPTS -jar /usr/share/repose/repose.jar -c /etc/repose && tail -f /var/log/repose/repose.log
