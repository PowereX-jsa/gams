package org.gams.integration;

import io.micronaut.context.annotation.Requires;
import io.micronaut.scheduling.annotation.Scheduled;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gams.integration.services.OptimizationService;

@Singleton
@RequiredArgsConstructor
@Slf4j
@Requires(notEnv = "test")
public class ScheduledOptimization {

  private final OptimizationService service;

  @Scheduled(fixedRate = "5m")
  public void optimize() {
    var freight = 1.1;
    log.info("running optimization with freight: {}", freight);

    service.runOptimization(freight);
  }
}
