package org.gams.integration.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3BucketEntity;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class RequestHandler {

  public static String handleRequest(S3Event event, Context context) {
    log.info("request received - event: '{}'", event.toString());

    return event.getRecords().stream()
        .map(s3EventNotificationRecord -> {
          log.info("notif record: '{}'", s3EventNotificationRecord.toString());

          S3BucketEntity bucket = s3EventNotificationRecord.getS3()
              .getBucket();
          String file = s3EventNotificationRecord.getS3().getObject().getKey();

          log.info("bucket: '{}'", bucket.toString());
          log.info("file: '{}'", file);

          return bucket.getName() + ": " + file;
        })
        .findFirst()
        .orElse("no bucket found");
  }

}
