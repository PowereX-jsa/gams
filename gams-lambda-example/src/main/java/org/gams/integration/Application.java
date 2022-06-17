package org.gams.integration;

import io.micronaut.runtime.Micronaut;

// TODO try micronaut serverless function -> could be faster and easier to deploy
public class Application {
    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}
