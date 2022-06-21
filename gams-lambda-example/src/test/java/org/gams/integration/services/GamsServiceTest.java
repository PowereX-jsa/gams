package org.gams.integration.services;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.gams.api.GAMSWorkspace;
import io.micronaut.context.annotation.Value;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import org.gams.integration.props.GamsProps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@MicronautTest
class GamsServiceTest {

  @Inject
  private GamsService gamsService;

  @Inject
  private GamsProps gamsProps;

  @Value("src/test/resources/gams-test-files/dbInVariables.gdx")
  private File dbInVariablesFile;

  @Value("src/test/resources/gams-test-files/GAMS_test_script.gms")
  private File gamsFile;

  @Test
  void getGetWorkingDirByTimestamp(@TempDir Path tempRootWorkDir) {
    gamsProps.setRootWorkingDir(tempRootWorkDir.toString());

    var timestamp = Instant.now();
    Path workDir = gamsService.getGetWorkingDirByTimestamp(timestamp);

    assertThat(workDir.toFile())
        .exists()
        .hasFileName(timestamp.toString());
  }

  @Test
  void runGamsTest(@TempDir Path tempRootWorkDir) {
    File output = gamsService.runGams(tempRootWorkDir, Collections.singletonList(dbInVariablesFile),
        gamsFile);

    assertThat(output)
        .exists()
        .hasFileName("dbOut.gdx");
  }
}
