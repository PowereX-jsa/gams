package org.gams.integration.services;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import jakarta.inject.Singleton;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;

@Singleton
@Slf4j
@RequiredArgsConstructor
// TODO tests
public class S3Service {

  // TODO via configuration
  private static final String INPUT_DATA_BUCKET_NAME = "data-input-lambda-test";
  private static final String OUTPUT_DATA_BUCKET_NAME = "data-output-lambda-test";

  public ImmutablePair<String, String> uploadFile(String folder, File file) {
    AmazonS3 client = createS3Client();

    String s3Key = Path.of(folder, file.getName()).toString();

    PutObjectResult result = client.putObject(
        OUTPUT_DATA_BUCKET_NAME, s3Key, file);

    log.info("successfully uploaded '{}' to s3 bucket: '{}' from local file: '{}'", s3Key,
        OUTPUT_DATA_BUCKET_NAME, file);
    log.debug("rawObjectMetadata='{}'", result.getMetadata().getRawMetadata());

    return new ImmutablePair<>(OUTPUT_DATA_BUCKET_NAME, s3Key);
  }

  public File downloadFile(String key, File targetFile) {
    AmazonS3 client = createS3Client();

    ObjectMetadata objectMetadata = client.getObject(
        new GetObjectRequest(INPUT_DATA_BUCKET_NAME, key), targetFile);
    log.info("successfully download '{}' from s3 bucket: '{}' to local file: '{}'", key,
        INPUT_DATA_BUCKET_NAME, targetFile);
    log.debug("rawObjectMetadata='{}'", objectMetadata.getRawMetadata());

    return targetFile;
  }

  public String getParentOfS3File(String s3KeyFile) {
    return Path.of(s3KeyFile).getParent().toString();
  }

  @SneakyThrows(IOException.class)
  public byte[] downloadFile(String key) {
    AmazonS3 client = createS3Client();

    S3Object object = client.getObject(new GetObjectRequest(INPUT_DATA_BUCKET_NAME, key));
    log.info("successfuly download '{}' from s3 bucket: '{}' to byte array", key,
        INPUT_DATA_BUCKET_NAME);
    log.debug("rawObjectMetadata='{}'", object.getObjectMetadata().getRawMetadata());

    InputStream inputStream = new BufferedInputStream(object.getObjectContent());

    return inputStream.readAllBytes();
  }

  // needs to have following env vars set (in idea via run configuration or in docker container):
  // AWS_ACCESS_KEY_ID
  // AWS_SECRET_ACCESS_KEY
  // AWS_SESSION_TOKEN
  private AmazonS3 createS3Client() {
    return AmazonS3ClientBuilder
        .standard()
        .withRegion(
            Regions.EU_CENTRAL_1) // TODO should be configurable (maybe via env var as in R apis?) - it`s different in dev/prod aws accounts
        .build();
  }
}
