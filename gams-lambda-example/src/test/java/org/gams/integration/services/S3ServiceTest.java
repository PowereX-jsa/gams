package org.gams.integration.services;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import io.micronaut.test.annotation.MockBean;
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
import org.gams.integration.utils.AwsS3Utils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

@MicronautTest
@Slf4j
class S3ServiceTest {

  @Inject
  private S3Service s3Service;

  @Inject
  private S3ClientService clientService;

  private static LocalStackContainer s3Container;

  private static final String TEST_JSON = "\"{\"test\": \"value\"}\"";

  @BeforeAll
  public static void setupServer() {
    s3Container = AwsS3Utils.runS3Container();
  }

  // needs to be closed after test run
  @AfterAll
  public static void stopServer() {
    AwsS3Utils.shutdownS3Container(s3Container);
  }

  @BeforeEach
  void setUpMocks() {
    lenient().when(clientService.getClient()).thenReturn(AwsS3Utils.getS3Client(s3Container));
  }

  @Test
  void getParentOfS3FileTest() {
    var file = "/gams-input-data_timestamp/wtf/manifest.json";

    assertThat(s3Service.getParentOfS3File(file)).isEqualTo("/gams-input-data_timestamp/wtf");
  }

  @Test
  void downloadFileTest(@TempDir Path tempDir) {
    String filename = "test.json";

    AwsS3Utils.uploadToS3(s3Container, S3Service.INPUT_DATA_BUCKET_NAME, filename, TEST_JSON);

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

    AwsS3Utils.uploadToS3(s3Container, S3Service.INPUT_DATA_BUCKET_NAME, filename, TEST_JSON);

    byte[] jsonAsBytes = s3Service.downloadFile(filename);

    assertThat(jsonAsBytes).asString().isEqualTo(TEST_JSON);
  }

  @Test
  @SneakyThrows(IOException.class)
  void uploadFileTest(@TempDir Path tempDir) {
    AwsS3Utils.createBucket(s3Container, S3Service.OUTPUT_DATA_BUCKET_NAME);

    String filename = "test.json";

    File file = Path.of(tempDir.toString(), filename).toFile();

    log.debug("writing to test json: '{}'", file.getAbsolutePath());
    Files.write(file.toPath(), TEST_JSON.getBytes(StandardCharsets.UTF_8));

    ImmutablePair<String, String> result = s3Service.uploadFile(
        tempDir.toString(), file);

    log.debug("upload result: '{}'", result);

    assertThat(result.getKey()).isEqualTo(S3Service.OUTPUT_DATA_BUCKET_NAME);
    assertThat(result.getValue()).isEqualTo(file.getAbsolutePath());

    assertThat(AwsS3Utils.downloadJsonFromS3(s3Container, S3Service.OUTPUT_DATA_BUCKET_NAME,
        result.getValue())).isEqualTo(TEST_JSON);
  }

  @MockBean(S3ClientService.class)
  S3ClientService s3ClientService() {
    return mock(S3ClientService.class);
  }
}
