package org.gams.integration.services;

import com.gams.api.GAMSVariable;
import io.micronaut.transaction.annotation.TransactionalAdvice;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.gams.integration.GamsVariables;
import org.gams.integration.persistence.OptimizationEntity;
import org.gams.integration.persistence.OptimizationRepository;

@Singleton
@RequiredArgsConstructor
public class OptimizationRepositoryService {

  private final OptimizationRepository repository;

  @TransactionalAdvice
  public List<OptimizationEntity> saveResult(Map<GamsVariables, GAMSVariable> result) {

    var entities = StreamSupport
        .stream(result.get(GamsVariables.OUTPUT_VARIABLE).spliterator(), false)
        .map(variable -> OptimizationEntity.builder()
            .optimizationStart(Instant.now())
            .level(BigDecimal.valueOf(variable.getLevel()))
            .margin(BigDecimal.valueOf(variable.getMarginal()))
            .build())
        .collect(Collectors.toList());

    return repository.saveAll(entities);
  }
}
