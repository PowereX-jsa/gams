FROM amazoncorretto:25

COPY --from=powerex/gams-init:51.4.0 /build/gams /opt/gams/

ENV GAMS_SYSTEM_DIR=/opt/gams
ENV PATH="${PATH}:/opt/gams"

WORKDIR /app

ENTRYPOINT ["gams"]
