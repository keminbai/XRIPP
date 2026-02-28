package com.xripp.backend;  // ← 包名必须是这个

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IppBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(IppBackendApplication.class, args);
    }

}