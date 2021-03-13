package org.gams.integration.persistence;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import java.time.Instant;

@Repository
public interface OptimizationRepository extends JpaRepository<OptimizationEntity, Instant> {

}
