package org.gams.integration.services;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.gams.integration.services.s3.S3Service;
import org.gams.integration.utils.LocalS3ClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@MicronautTest
@Slf4j
class S3ServiceTest {

  @Inject
  private S3Service s3Service;

  @Inject
  private LocalS3ClientService s3Client;

  private static final String TEST_JSON = "\"{\"test\": \"value\"}\"";

  @Test
  void getParentOfS3FileTest() {
    var file = "/gams-input-data_timestamp/wtf/manifest.json";

    assertThat(s3Service.getParentOfS3File(file)).isEqualTo("/gams-input-data_timestamp/wtf");
  }

  @Test
  void downloadFileTest(@TempDir Path tempDir) {
    String filename = "test.json";

    s3Client.uploadToS3(S3Service.INPUT_DATA_BUCKET_NAME, filename, TEST_JSON);

    File file = Path.of(tempDir.toString(), filename).toFile();

    file = s3Service.downloadFile(filename, file);

    assertThat(file)
        .exists()
        .isFile()
        .hasFileName(filename)
        .hasContent(TEST_JSON);
  }

  @Test
  void downloadFileBytesTest() {
    String filename = "test.json";

    s3Client.uploadToS3(S3Service.INPUT_DATA_BUCKET_NAME, filename, TEST_JSON);

    byte[] jsonAsBytes = s3Service.downloadFile(filename);

    assertThat(jsonAsBytes).asString().isEqualTo(TEST_JSON);
  }

  @Test
  @SneakyThrows(IOException.class)
  void uploadFileTest(@TempDir Path tempDir) {
    s3Client.createBucket(S3Service.OUTPUT_DATA_BUCKET_NAME);

    String filename = "test.json";

    File file = Path.of(tempDir.toString(), filename).toFile();

    log.debug("writing to test json: '{}'", file.getAbsolutePath());
    Files.write(file.toPath(), TEST_JSON.getBytes(StandardCharsets.UTF_8));

    ImmutablePair<String, String> result = s3Service.uploadFile(
        tempDir.toString(), file);

    log.debug("upload result: '{}'", result);

    assertThat(result.getKey()).isEqualTo(S3Service.OUTPUT_DATA_BUCKET_NAME);
    assertThat(result.getValue()).isEqualTo(file.getAbsolutePath());

    assertThat(s3Client.downloadJsonFromS3(S3Service.OUTPUT_DATA_BUCKET_NAME,
        result.getValue())).isEqualTo(TEST_JSON);
  }
}
