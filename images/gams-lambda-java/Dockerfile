FROM powerex/gams-init:latest

FROM public.ecr.aws/lambda/java:17

COPY --from=0 /opt/gams/gamsFiles/linux_x64_64_sfx.exe ./

RUN chmod +x ./linux_x64_64_sfx.exe
RUN ./linux_x64_64_sfx.exe

# check install dir
RUN pwd
RUN ls -la .

# export gams directory as env var for future application use
ENV GAMS_SYSTEM_DIR /var/task/gams33.2_linux_x64_64_sfx

# remove installation file 
RUN rm ./linux_x64_64_sfx.exe
