FROM powerex/gams-init:latest

FROM public.ecr.aws/lambda/java:11

RUN mkdir ./gams

COPY --from=0 /opt/gams/gamsFiles/linux_x64_64_sfx.exe ./gams/

RUN ls -la "./gams"

RUN chmod +x ./gams/linux_x64_64_sfx.exe
RUN ./gams/linux_x64_64_sfx.exe
RUN rm ./gams/linux_x64_64_sfx.exe
