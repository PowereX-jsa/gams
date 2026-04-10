FROM ubuntu:26.04

WORKDIR /build
ARG GAMS_INSTALL_FILE

# Install GAMS
COPY $GAMS_INSTALL_FILE .
RUN chmod +x ${GAMS_INSTALL_FILE}
RUN ${GAMS_INSTALL_FILE}
RUN rm ${GAMS_INSTALL_FILE}
# use symbolic link to simplify path for another Dockerfile
RUN ln -s /build/gams*/ /build/gams
