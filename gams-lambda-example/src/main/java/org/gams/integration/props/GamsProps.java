package org.gams.integration.props;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;

@ConfigurationProperties("gams")
@Data
public class GamsProps {

  private String systemDir;
  private String rootWorkingDir;
}
