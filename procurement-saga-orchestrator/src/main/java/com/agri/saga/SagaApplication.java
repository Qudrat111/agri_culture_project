package com.agri.saga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class SagaApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SagaApplication.class, args);
    }
}
