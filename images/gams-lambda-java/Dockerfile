FROM powerex/gams-init:44.2.0

FROM public.ecr.aws/lambda/java:17

COPY --from=0 /opt/gams/gamsFiles/linux_x64_64_sfx.exe ./

RUN chmod +x ./linux_x64_64_sfx.exe
RUN ./linux_x64_64_sfx.exe

# check install dir
RUN pwd
RUN ls -la .

# export gams directory as env var for future application use
RUN ls -la ${LAMBDA_TASK_ROOT}
ENV GAMS_SYSTEM_DIR ${LAMBDA_TASK_ROOT}/gams44.2_linux_x64_64_sfx

# copy gams api files
RUN mkdir ${LAMBDA_TASK_ROOT}/lib/
RUN cp -R ${GAMS_SYSTEM_DIR}/apifiles/Java/api/* ${LAMBDA_TASK_ROOT}/lib/
RUN ls -la ${LAMBDA_TASK_ROOT}/lib/

# remove installation file 
RUN rm ./linux_x64_64_sfx.exe
