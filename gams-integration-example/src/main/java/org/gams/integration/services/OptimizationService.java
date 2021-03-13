package org.gams.integration.services;

import com.gams.api.GAMSVariable;
import com.gams.api.GAMSVariableRecord;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gams.integration.GamsVariables;

@Singleton
@RequiredArgsConstructor
@Slf4j
public class OptimizationService {

  private final GamsModelService gamsModelService;
  private final OptimizationRepositoryService optimizationRepositoryService;

  public void runOptimization(double freight) {
    gamsModelService.fillInputDb(freight);

    Map<GamsVariables, GAMSVariable> result = gamsModelService
        .optimize();

    result.forEach((key, value) -> {
      var values = StreamSupport.stream(value.spliterator(), false)
          .map(GAMSVariableRecord::getLevel).collect(Collectors.toList());
      log.info("key: {}, level values: {}", key, values);
    });

    var savedEntities = optimizationRepositoryService.saveResult(result);

    log.info("Successfully saved: {}", savedEntities);

    gamsModelService.clearInputDb();
    gamsModelService.disposeOutputDb();
  }

}
