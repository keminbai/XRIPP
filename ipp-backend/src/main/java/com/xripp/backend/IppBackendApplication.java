package com.xripp.backend;  // ← 包名必须是这个

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IppBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(IppBackendApplication.class, args);
    }

}