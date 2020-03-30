package com.redroundrobin.thirema.apirest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication

public class ApirestApplication {

  public static void main(String[] args) {
    SpringApplication.run(ApirestApplication.class, args);
  }

   // Proxy di avvio: https://github.com/spring-projects/spring-boot/issues/4779#issuecomment-305482782
}