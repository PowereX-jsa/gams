FROM powerex/gams-init:latest

FROM public.ecr.aws/lambda/java:17

RUN mkdir /opt/gams

COPY --from=0 /opt/gams/gamsFiles/linux_x64_64_sfx.exe /opt/gams

RUN chmod +x /opt/gams/linux_x64_64_sfx.exe
RUN /opt/gams/linux_x64_64_sfx.exe

# TODO export gams system dir as env var
RUN ls -la "/opt/gams"
RUN whereis gams

# remove installation file 
RUN rm /opt/gams/linux_x64_64_sfx.exe
