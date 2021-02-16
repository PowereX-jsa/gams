FROM ubuntu:20.04

USER root

SHELL ["/bin/bash", "-c"]

COPY gamsWorkingDir/gamsInstallDir /opt/gams
COPY gamsWorkingDir/gamsJavaApi /opt/gams/gamsJavaApi

# install gams
WORKDIR /opt/gams
RUN /opt/gams/linux_x64_64_sfx.exe
RUN cp /opt/gams/gamslice.txt /opt/gams/gams33.2_linux_x64_64_sfx/gamslice.txt
RUN mkdir /opt/gams/working-dir

# remove install file
RUN rm /opt/gams/linux_x64_64_sfx.exe
