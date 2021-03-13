package org.gams.integration.services;

import com.gams.api.GAMSDatabase;
import com.gams.api.GAMSOptions;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.gams.integration.GamsVariables;

@Singleton
@Slf4j
public class GamsModelSetupService {

  public void setUpGamsOptions(GAMSOptions options) {
    options.setSolveLink(GAMSOptions.ESolveLink.LoadLibrary);
    setDefaultSolvers(options);

    // input dbs
    options.defines("dbIn", "dbIn");
    // output db for results
    options.defines("dbOut", "dbOut");
  }

  public void setUpInputDb(GAMSDatabase inputDb) {
    inputDb
        .addParameter(GamsVariables.FREIGHT_VARIABLE.value,
            "freight in dollars per case per thousand miles");
  }

  private void setDefaultSolvers(GAMSOptions options) {
    log.info("setting default solvers");

    options.setLP("CONOPT");
    options.setMIP("CPLEX");
    options.setRMINLP("CONOPT");
    options.setNLP("CONOPT");
    options.setMCP("MILES");
    options.setMPEC("NLPEC");
    options.setRMPEC("NLPEC");
    options.setCNS("CONOPT");
    options.setDNLP("CONOPT");
    options.setRMINLP("CONOPT");
    options.setMINLP("SBB");
    options.setQCP("CONOPT");
    options.setMIQCP("SBB");
    options.setRMIQCP("CONOPT");
    options.setEMP("JAMS");
  }
}
