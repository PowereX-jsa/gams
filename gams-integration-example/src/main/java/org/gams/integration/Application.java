package org.gams.integration;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.info.*;
import java.sql.SQLException;
import org.h2.tools.Server;

@OpenAPIDefinition(
    info = @Info(
        title = "gams-integration",
        version = "0.0"
    )
)
public class Application {

  public static void main(String[] args) throws SQLException {

    // workaround for enabling h2 console in micronaut: https://github.com/micronaut-projects/micronaut-core/issues/1004
    // console runs on: 'localhost:8082'
    Server.createWebServer().start();

    Micronaut.run(Application.class, args);
  }
}
