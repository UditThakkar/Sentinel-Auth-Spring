package com.udit.authlib;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AuthLibApplication {

  public static void main(String[] args) {
    SpringApplication.run(AuthLibApplication.class, args);
  }

}
