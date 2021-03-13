package org.gams.integration.persistence;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import java.math.BigDecimal;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest
class OptimizationRepositoryTest {

  @Inject
  private OptimizationRepository repository;

  @Test
  void persistTest() {
    var entity = new OptimizationEntity();
    entity.setLevel(BigDecimal.valueOf(10.0));
    entity.setMargin(BigDecimal.valueOf(11.0));

    assertThat(repository.findAll()).isEmpty();

    repository.saveAndFlush(entity);

    assertThat(repository.findAll()).containsOnly(entity);
  }

}