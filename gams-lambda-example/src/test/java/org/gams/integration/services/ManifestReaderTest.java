package org.gams.integration.services;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@MicronautTest
class ManifestReaderTest {

  @Inject
  private ManifestReader manifestReader;

  @Inject
  private S3Service s3Service;

  @Test
  void getFilenamesFromManifestTest() {
    final var s3FileLocation = "s3location/manifest.json";

    final var manifestContent = "[\"dbInVariables.gdx\", \"dbInConstant.gdx\", \"gams-file.gms\"]";

    when(s3Service.downloadFile(s3FileLocation)).thenReturn(manifestContent.getBytes(
        StandardCharsets.UTF_8));

    assertThat(manifestReader.getFilenamesFromManifest(s3FileLocation))
        .containsExactly("dbInVariables.gdx", "dbInConstant.gdx", "gams-file.gms");
  }

  @MockBean(S3Service.class)
  S3Service s3Service() {
    return mock(S3Service.class);
  }

}
