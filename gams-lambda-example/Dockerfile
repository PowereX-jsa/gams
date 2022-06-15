FROM powerex/gams-lambda-java

COPY build/libs/* ${LAMBDA_TASK_ROOT}/lib/

RUN ls -la ${LAMBDA_TASK_ROOT}/lib/

CMD ["org.gams.integration.handlers.RequestHandler::execute"]
