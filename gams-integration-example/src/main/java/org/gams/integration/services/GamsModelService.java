package org.gams.integration.services;

import com.gams.api.GAMSDatabase;
import com.gams.api.GAMSVariable;
import java.util.Map;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.gams.integration.GamsModel;
import org.gams.integration.GamsVariables;

@Singleton
@RequiredArgsConstructor
public class GamsModelService {

  private final GamsModel model;

  public void fillInputDb(double freight) {
    model.getDbIn().getParameter(GamsVariables.FREIGHT_VARIABLE.value).addRecord()
        .setValue(freight);
  }

  public Map<GamsVariables, GAMSVariable> optimize() {

    model.run(System.out);

    GAMSDatabase dbOut = model.getDbOut();

    return Map
        .of(GamsVariables.OUTPUT_VARIABLE, dbOut.getVariable(GamsVariables.OUTPUT_VARIABLE.value));
  }

  public void disposeOutputDb() {
    model.getDbOut().dispose();
  }

  public void clearInputDb() {
    model.getDbIn().clear();
  }
}
