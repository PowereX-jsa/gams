FROM powerex/gams-init:44.2.0

USER root

SHELL ["/bin/bash", "-c"]

# git install
RUN apt-get update -y
RUN apt-get install -y git

# copy gams files
COPY ./gams-install-dir/gamsFiles/docker-install-java.sh /opt/gams/gamsFiles/docker-install-java.sh

# install gams
WORKDIR /opt/gams
RUN ls -la /opt/gams
RUN ls -la /opt/gams/gamsFiles
RUN chmod +x /opt/gams/gamsFiles/linux_x64_64_sfx.exe
RUN /opt/gams/gamsFiles/linux_x64_64_sfx.exe

ENV GAMS_SYSTEM_DIR /opt/gams/gams44.2_linux_x64_64_sfx

COPY ./gams-install-dir/gamsFiles/test-file.gms ${GAMS_SYSTEM_DIR}/test-file.gms

# remove install file
RUN rm /opt/gams/gamsFiles/linux_x64_64_sfx.exe

# install java via sdkman
RUN apt-get update -y && apt-get install -y --no-install-recommends wget curl zip unzip ca-certificates
RUN mkdir /opt/sdkman
WORKDIR /opt/sdkman

RUN curl "https://get.sdkman.io" | bash
RUN chmod a+x "$HOME/.sdkman/bin/sdkman-init.sh"
RUN source "$HOME/.sdkman/bin/sdkman-init.sh"

# workaround for current bash session in docker build
RUN chmod +x /opt/gams/gamsFiles/docker-install-java.sh
RUN /opt/gams/gamsFiles/docker-install-java.sh

ENV JAVA_HOME /root/.sdkman/candidates/java/current/bin/java
RUN ln -s /root/.sdkman/candidates/java/current/bin/java /bin/java
