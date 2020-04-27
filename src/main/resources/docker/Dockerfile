ARG JDK_IMAGE=openjdk
ARG JDK_VERSION=8u181-jre
FROM ${JDK_IMAGE}:${JDK_VERSION}
ENV JVM_OPTIONS=-Xmx256m
COPY *.sh /data/
RUN groupadd -g 1000 -r sld \
 && useradd -u 1000 -g 1000 -r -m sld \
 && chmod +x /data/*.sh \
 && mkdir /var/lm \
 && chown sld:sld /var/lm /data
USER sld
COPY sol003-lifecycle-driver-@project.version@.jar /data/sol003-lifecycle-driver-@project.version@.jar
CMD sh /data/commands.sh