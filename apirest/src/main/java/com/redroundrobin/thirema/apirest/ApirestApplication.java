package com.redroundrobin.thirema.apirest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;


@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class ApirestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApirestApplication.class, args);
    }

}
