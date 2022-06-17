package org.gams.integration.handlers;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3BucketEntity;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.function.aws.MicronautRequestHandler;
import jakarta.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.gams.integration.services.GamsService;
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

  @Inject
  private GamsService gamsService;

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

          String manifestParentFolder = s3Service.getParentOfS3File(manifestFileKey);
          log.info("manifest: '{}' from: {}", manifestFileKey, manifestParentFolder);

          List<String> filenames = manifestReader.getFilenamesFromManifest(
              manifestFileKey);

          // TODO add clock
          Path currentGamsWorkDir = gamsService.getGetWorkingDirByTimestamp(Instant.now());

          List<File> files = filenames.stream()
              .map(s3Key -> new ImmutablePair<>(s3Key,
                  getLocalFileForS3File(currentGamsWorkDir, s3Key)))
              .map(s3KeyLocalFilePair -> s3Service.downloadFile(s3KeyLocalFilePair.getKey(),
                  s3KeyLocalFilePair.getValue()))
              .collect(Collectors.toUnmodifiableList());

          List<File> gdxFiles = files.stream()
              .filter(file -> file.getName().endsWith(".gdx"))
              .collect(Collectors.toUnmodifiableList());

          File gmsFile = files.stream()
              .filter(file -> file.getName().endsWith(".gms"))
              .findFirst()
              .orElseThrow(() -> new IllegalStateException("no gams script found"));

          File gamsOutputFile = gamsService.runGams(currentGamsWorkDir, gdxFiles, gmsFile);

          ImmutablePair<String, String> s3FileLocation = s3Service.uploadFile(manifestParentFolder,
              gamsOutputFile);

          return s3FileLocation.getKey() + ": " + s3FileLocation.getValue();
        })
        .orElseThrow(() -> new IllegalStateException("no record in input events"));
  }

  private File getLocalFileForS3File(Path workDir, String s3Key) {
    String s3Filename = Path.of(s3Key).getFileName().toString();
    log.trace("s3Filename='{}'", s3Filename);

    return Path.of(workDir.toAbsolutePath().toString(), s3Filename).toFile();
  }
}
