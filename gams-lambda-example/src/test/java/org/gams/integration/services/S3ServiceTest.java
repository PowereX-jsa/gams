package org.gams.integration.services;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest
class S3ServiceTest {

  @Inject
  private S3Service s3Service;

  @Test
  void getParentOfS3FileTest() {
    var file = "/gams-input-data_timestamp/wtf/manifest.json";

    assertThat(s3Service.getParentOfS3File(file)).isEqualTo("/gams-input-data_timestamp/wtf");
  }
}
