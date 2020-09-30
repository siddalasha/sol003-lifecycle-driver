ARG JDK_IMAGE=openjdk
ARG JDK_VERSION=8u181-jre
FROM ${JDK_IMAGE}:${JDK_VERSION}
ENV JVM_OPTIONS=-Xmx256m
COPY *.sh /data/
RUN groupadd -g 1000 -r sld \
 && useradd -u 1000 -g 1000 -r -m sld \
 && chmod +x /data/*.sh \
 && mkdir /var/lm \
 # set group to 0 to allow arbitrary users in OCP to write to this directory
 && chown -R sld:0 /var/lm /data
USER sld
COPY sol003-lifecycle-driver-@project.version@.jar /data/sol003-lifecycle-driver-@project.version@.jar
CMD sh /data/commands.sh