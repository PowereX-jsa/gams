FROM powerex/gams-lambda-java

COPY build/classes/java/main ${LAMBDA_TASK_ROOT}
COPY build/libs/* ${LAMBDA_TASK_ROOT}/lib/

CMD ["org.gams.integration.handlers.RequestHandler::handleRequest"]
