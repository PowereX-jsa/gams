package org.gams.integration.services;

import static org.mockito.Mockito.mock;

import com.gams.api.GAMSOptions;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@MicronautTest
class GamsModelSetupServiceTest {

  @Inject
  private GamsModelSetupService service;

  @Test
  void setUpGamsOptionsTest() {
    GAMSOptions gamsOptions = mock(GAMSOptions.class);

    service.setUpGamsOptions(gamsOptions);

    Mockito.verify(gamsOptions, Mockito.times(1)).setSolveLink(GAMSOptions.ESolveLink.LoadLibrary);
    Mockito.verify(gamsOptions, Mockito.times(1)).defines("dbIn", "dbIn");
    Mockito.verify(gamsOptions, Mockito.times(1)).defines("dbOut", "dbOut");
  }
}