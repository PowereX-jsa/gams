package org.gams.integration.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Entity
@Table(name = "gams_optimization")
@AllArgsConstructor
@Data
@Builder
public class OptimizationEntity {

  @Id
  private Instant optimizationStart;

  private BigDecimal level;
  private BigDecimal margin;

  public OptimizationEntity() {
    this.optimizationStart = Instant.now().truncatedTo(ChronoUnit.MILLIS);
  }
}
