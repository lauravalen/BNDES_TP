package com.example.saftbndes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SaftBndesApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaftBndesApplication.class, args);
        System.out.println("===========================================");
        System.out.println("  SAFT-BNDES rodando!");
        System.out.println("  API:        http://localhost:8080");
        System.out.println("  H2 Console: http://localhost:8080/h2-console");
        System.out.println("===========================================");
    }
}