package org.gams.integration.handlers;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3BucketEntity;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.function.aws.MicronautRequestHandler;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gams.integration.services.ManifestReader;
import org.gams.integration.services.S3Service;

@Slf4j
@Introspected
@RequiredArgsConstructor
public class RequestHandler extends
    MicronautRequestHandler<S3Event, String> {

  private final S3Service s3Service;
  private final ManifestReader manifestReader;

  @Override
  public String execute(S3Event input) {
    log.info("request received - event: '{}'", input.toString());

    return Optional.ofNullable(input.getRecords().get(0))
        .map(s3EventNotificationRecord -> {
          log.info("notif record: '{}'", s3EventNotificationRecord.toString());

          S3BucketEntity bucket = s3EventNotificationRecord.getS3()
              .getBucket();
          String manifestFileKey = s3EventNotificationRecord.getS3().getObject().getKey();

          log.info("bucket: '{}'", bucket.toString());
          log.info("manifest: '{}'", manifestFileKey);

          List<String> filenames = manifestReader.getFilenamesFromManifest(
              manifestFileKey);

          filenames.forEach(filenameS3Key -> {

            Path localFilePath = Path.of("/tmp", Path.of(filenameS3Key).getFileName().toString());
            s3Service.downloadFile(filenameS3Key, localFilePath.toFile());
          });

          return bucket.getName() + ": " + manifestFileKey;
        })
        .orElse("no bucket found");
  }
}
