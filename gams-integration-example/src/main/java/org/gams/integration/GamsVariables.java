package org.gams.integration;

public enum GamsVariables {
  OUTPUT_VARIABLE("x"),
  FREIGHT_VARIABLE("f");

  public final String value;

  GamsVariables(String variable) {
    this.value = variable;
  }
}
