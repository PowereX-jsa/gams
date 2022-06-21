package org.gams.integration.handlers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3BucketEntity;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3Entity;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3ObjectEntity;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import io.micronaut.context.annotation.Value;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.gams.integration.services.s3.S3Service;
import org.gams.integration.utils.LocalS3ClientService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


@MicronautTest
@Slf4j
class RequestHandlerTest {

  @Inject
  private LocalS3ClientService s3ClientService;

  @Inject
  EmbeddedApplication<?> application;

  private static RequestHandler handler;

  @Value("src/test/resources/gams-test-files/dbInVariables.gdx")
  private File dbInVariablesFile;

  @Value("src/test/resources/gams-test-files/GAMS_test_script.gms")
  private File gamsFile;

  @Value("src/test/resources/gams-test-files/manifest.json")
  private File manifestFile;

  private static final String BASE_DIR = "gams-test-data_2022-06-09T12:37:08.047781Z/";

  // app context start when you instantiate handler
  @BeforeEach
  public void setupServer() {
    uploadTestFiles();

    handler = new RequestHandler(application.getApplicationContext());
  }

  // needs to be closed after test run
  @AfterEach
  public void stopServer() {
    if (handler != null) {
      handler.getApplicationContext().close();
    }
  }

  @Test
  @SneakyThrows
  void testHandler() {

    // TODO mock only client, upload files, trigger event (execute), check output gams file

    // mocking relevant content for s3 put event
    S3Event event = mock(S3Event.class);
    S3EventNotificationRecord eventRecord = mock(S3EventNotificationRecord.class);
    S3Entity s3Entity = mock(S3Entity.class);
    S3BucketEntity s3BucketEntityEntity = mock(S3BucketEntity.class);
    S3ObjectEntity s3ObjectEntity = mock(S3ObjectEntity.class);

    when(event.getRecords()).thenReturn(Collections.singletonList(eventRecord));
    when(eventRecord.getS3()).thenReturn(s3Entity);
    when(s3Entity.getBucket()).thenReturn(s3BucketEntityEntity);
    when(s3Entity.getObject()).thenReturn(s3ObjectEntity);
    when(s3ObjectEntity.getKey()).thenReturn(BASE_DIR + "manifest.json");

    String response = handler.execute(event);

    assertThat(response).isNotNull().isNotBlank().isNotEmpty();

    String outputFileKey = s3ClientService.getClient()
        .listObjects(S3Service.OUTPUT_DATA_BUCKET_NAME)
        .getObjectSummaries()
        .stream()
        .findFirst()
        .map(S3ObjectSummary::getKey)
        .orElseThrow();

    assertThat(outputFileKey).isEqualTo(BASE_DIR + "dbOut.gdx");
  }

  private void uploadTestFiles() {
    Arrays.asList(gamsFile, dbInVariablesFile, manifestFile)
        .forEach(
            file -> {
              String s3Key = BASE_DIR + file.getName();
              log.debug("uploading to input bucket with s3 key: '{}'", s3Key);

              s3ClientService.uploadToS3(S3Service.INPUT_DATA_BUCKET_NAME, s3Key, file);
            }
        );
  }
}
