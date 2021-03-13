package org.gams.integration.configuration;

import com.gams.api.GAMSWorkspace;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import java.nio.file.Path;
import org.gams.integration.GamsModel;
import org.gams.integration.services.GamsModelSetupService;
import org.springframework.beans.factory.annotation.Value;

@Factory
public class GamsModelFactory {

  @Value("${gams.model-file}")
  private String gamsOptimizationModelFilePath;

  @Bean
  public GamsModel gamsOptimizationModel(GAMSWorkspace ws, GamsModelSetupService setUpService) {
    var model = new GamsModel(ws, Path.of(gamsOptimizationModelFilePath));
    setUpService.setUpGamsOptions(model.getOpt());
    setUpService.setUpInputDb(model.getDbIn());

    return model;
  }
}
