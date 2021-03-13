package org.gams.integration.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import javax.inject.Singleton;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Singleton
@ConfigurationProperties("gams.workspace")
@Data
public class GamsConfig {

  @NotBlank
  private String systemDir;

  @NotBlank
  private String workingDir;
}
