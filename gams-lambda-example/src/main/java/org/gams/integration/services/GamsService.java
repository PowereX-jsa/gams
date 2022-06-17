package org.gams.integration.services;

import com.gams.api.GAMSDatabase;
import com.gams.api.GAMSGlobals.DebugLevel;
import com.gams.api.GAMSJob;
import com.gams.api.GAMSOptions;
import com.gams.api.GAMSWorkspace;
import com.gams.api.GAMSWorkspaceInfo;
import jakarta.inject.Singleton;
import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gams.integration.props.GamsProps;

@Singleton
@Slf4j
@RequiredArgsConstructor
public class GamsService {

  private final GamsProps gamsProps;

  private static final String GAMS_DB_OUTPUT_NAME = "dbOut";
  private static final String GAMS_DB_SUFFIX = ".gdx";


  private static final String SOLVER_CONOPT = "CONOPT";
  private static final String SOLVER_CPLEX = "CPLEX";
  private static final String SOLVER_MILES = "MILES";
  private static final String SOLVER_NLPEC = "NLPEC";
  private static final String SOLVER_SBB = "SBB";
  private static final String SOLVER_JAMS = "JAMS";

  public Path getGetWorkingDirByTimestamp(Instant timestamp) {
    File workdir = Path.of(gamsProps.getRootWorkingDir(), timestamp.toString()).toFile();

    if (!workdir.exists()) {
      workdir.mkdirs();
    }

    return workdir.toPath().toAbsolutePath();
  }

  // TODO maybe only 1 workspace initialization?
  public File runGams(Path workDir, List<File> gdxFiles, File gamsScript) {
    GAMSWorkspace ws = getGamsWorkspace(workDir);

    GAMSOptions gamsOptions = setUpGamsOptions(ws);

    GAMSDatabase[] inputDatabases = addGdxFilesToWorkspace(ws, gdxFiles);

    GAMSJob gamsJob = setUpJob(ws, gamsScript);

    return run(ws, gamsOptions, gamsJob, inputDatabases);
  }

  private File run(GAMSWorkspace ws, GAMSOptions gamsOptions, GAMSJob gamsJob,
      GAMSDatabase[] gamsDatabases) {
    var startTime = System.currentTimeMillis();
    gamsJob.run(gamsOptions, null, System.out, false, gamsDatabases);

    ws.addDatabaseFromGDX(
        gamsOptions.getDefinitionOf(GAMS_DB_OUTPUT_NAME) + GAMS_DB_SUFFIX);
    var endTime = System.currentTimeMillis();

    log.info("gams run took: {}ms, done", endTime - startTime);

    return Path.of(ws.workingDirectory(), GAMS_DB_OUTPUT_NAME + GAMS_DB_SUFFIX).toFile();
  }

  private GAMSJob setUpJob(GAMSWorkspace ws, File gamsScript) {
    log.info("adding gams job, source code file: {}", gamsScript);
    return ws.addJobFromFile(gamsScript.getAbsolutePath());
  }

  private GAMSWorkspace getGamsWorkspace(Path workDir) {
    return new GAMSWorkspace(
        (new GAMSWorkspaceInfo(workDir.toAbsolutePath().toString(), gamsProps.getSystemDir(),
            DebugLevel.OFF)));
  }

  private GAMSOptions setUpGamsOptions(GAMSWorkspace ws) {
    GAMSOptions gamsOptions = ws.addOptions();

    gamsOptions.setSolveLink(GAMSOptions.ESolveLink.LoadLibrary);

    setDefaultSolvers(gamsOptions);

    defineOutputDb(gamsOptions);

    return gamsOptions;
  }

  private GAMSDatabase[] addGdxFilesToWorkspace(GAMSWorkspace ws, List<File> gdxFiles) {
    return gdxFiles.stream()
        .map(File::getAbsolutePath)
        .map(ws::addDatabaseFromGDX)
        .toArray(GAMSDatabase[]::new);
  }

  private void defineOutputDb(GAMSOptions gamsOptions) {
    gamsOptions.defines(GAMS_DB_OUTPUT_NAME, GAMS_DB_OUTPUT_NAME);
  }

  //@formatter:off

  /**
   * Installed defaults:
   * <p>
   * LP: CONOPT MIP: CPLEX RMIP: CONOPT NLP: CONOPT MCP: MILES MPEC: NLPEC RMPEC: NLPEC CNS: CONOPT
   * DNLP: CONOPT RMINLP: CONOPT MINLP: SBB QCP: CONOPT MIQCP: SBB RMIQCP: CONOPT EMP: JAMS
   */

  //@formatter:on
  private void setDefaultSolvers(GAMSOptions gamsOptions) {
    log.info("setting default solvers");

    gamsOptions.setLP(SOLVER_CONOPT);
    gamsOptions.setMIP(SOLVER_CPLEX);
    gamsOptions.setRMINLP(SOLVER_CONOPT);
    gamsOptions.setNLP(SOLVER_CONOPT);
    gamsOptions.setMCP(SOLVER_MILES);
    gamsOptions.setMPEC(SOLVER_NLPEC);
    gamsOptions.setRMPEC(SOLVER_NLPEC);
    gamsOptions.setCNS(SOLVER_CONOPT);
    gamsOptions.setDNLP(SOLVER_CONOPT);
    gamsOptions.setRMINLP(SOLVER_CONOPT);
    gamsOptions.setMINLP(SOLVER_SBB);
    gamsOptions.setQCP(SOLVER_CONOPT);
    gamsOptions.setMIQCP(SOLVER_SBB);
    gamsOptions.setRMIQCP(SOLVER_CONOPT);
    gamsOptions.setEMP(SOLVER_JAMS);
  }
}
