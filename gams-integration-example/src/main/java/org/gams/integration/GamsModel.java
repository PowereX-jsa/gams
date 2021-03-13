package org.gams.integration;

import com.gams.api.GAMSCheckpoint;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import com.gams.api.GAMSDatabase;
import com.gams.api.GAMSJob;
import com.gams.api.GAMSOptions;
import com.gams.api.GAMSWorkspace;
import org.springframework.util.StreamUtils;

@Data
@Slf4j
public class GamsModel {

  private final GAMSJob job;

  private final GAMSWorkspace ws;

  private final GAMSOptions opt;

  private final GAMSDatabase dbIn;

  private GAMSDatabase dbOut;

  @SneakyThrows
  public GamsModel(GAMSWorkspace ws, Path gamsOptimizationModelFilePath) {
    this.ws = ws;
    this.opt = ws.addOptions();

    this.dbIn = ws.addDatabase("dbIn");
    this.dbOut = ws.addDatabase("dbOut");

    log.info("adding gams job, source code file: {}", gamsOptimizationModelFilePath);

    this.job = ws
        .addJobFromString(
            StreamUtils.copyToString(Files.newInputStream(gamsOptimizationModelFilePath),
                StandardCharsets.UTF_8));
  }

  public void run(PrintStream output) {
    GAMSCheckpoint checkpoint = null;

    var startTime = System.currentTimeMillis();
    GAMSDatabase[] gamsDatabases = new GAMSDatabase[]{this.dbIn};
    job.run(this.opt, checkpoint, output, false, gamsDatabases);

    this.dbOut = ws.addDatabaseFromGDX(opt.getDefinitionOf("dbOut") + ".gdx");
    var endTime = System.currentTimeMillis();

    log.info("optimization took: {}ms, done", endTime - startTime);
  }
}
