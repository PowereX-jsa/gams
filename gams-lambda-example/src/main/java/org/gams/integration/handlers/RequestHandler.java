package org.gams.integration.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class RequestHandler {

  public static String handleRequest(S3Event event, Context context) {
    log.info("request received - event: '{}'", event);

    return "ok";
  }

}
