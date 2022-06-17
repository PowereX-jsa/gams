package org.gams.integration.handlers;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3BucketEntity;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.function.aws.MicronautRequestHandler;
import jakarta.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gams.integration.services.ManifestReader;
import org.gams.integration.services.S3Service;

@Slf4j
@Introspected
// TODO try native build for graalvm -> should be much faster
// TODO tests like this: https://guides.micronaut.io/latest/mn-serverless-function-aws-lambda-gradle-java.html
public class RequestHandler extends
    MicronautRequestHandler<S3Event, String> {

  @Inject
  private S3Service s3Service;

  @Inject
  private ManifestReader manifestReader;

  @Override
  public String execute(S3Event input) {
    log.info("request received - event: '{}'", input.toString());

    return input.getRecords().stream()
        .findFirst()
        .map(s3EventNotificationRecord -> {
          log.info("notif record: '{}'", s3EventNotificationRecord.toString());

          S3BucketEntity bucket = s3EventNotificationRecord.getS3()
              .getBucket();
          String manifestFileKey = s3EventNotificationRecord.getS3().getObject().getKey();

          log.info("bucket: '{}'", bucket.toString());
          log.info("manifest: '{}'", manifestFileKey);

          List<String> filenames = manifestReader.getFilenamesFromManifest(
              manifestFileKey);

          filenames.forEach(s3Key -> s3Service.downloadFile(s3Key, getLocalFileForS3File(s3Key)));

          return bucket.getName() + ": " + manifestFileKey;
        })
        .orElse("no bucket found");
  }

  private File getLocalFileForS3File(String s3Key) {
    String s3Filename = Path.of(s3Key).getFileName().toString();
    log.trace("s3Filename='{}'", s3Filename);

    // TODO should be created in gams workdir
    return Path.of("/tmp", s3Filename).toFile();
  }
}
