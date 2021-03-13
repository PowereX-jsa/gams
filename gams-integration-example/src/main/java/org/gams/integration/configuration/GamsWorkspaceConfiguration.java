package org.gams.integration.configuration;

import com.gams.api.GAMSWorkspace;
import com.gams.api.GAMSWorkspaceInfo;
import lombok.extern.slf4j.Slf4j;
import org.gams.integration.config.GamsConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class GamsWorkspaceConfiguration {

  @Bean
  public GAMSWorkspace configureWorkspace(GamsConfig config) {
    log.info("configuring gams workspace with following config: {}", config);

    GAMSWorkspaceInfo wsInfo = new GAMSWorkspaceInfo();
    wsInfo.setSystemDirectory(config.getSystemDir());
    wsInfo.setWorkingDirectory(config.getWorkingDir());

    return new GAMSWorkspace(wsInfo);
  }
}
