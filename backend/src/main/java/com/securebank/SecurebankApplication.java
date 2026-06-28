package com.securebank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
@EnableKafka
@EnableCaching
public class SecurebankApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecurebankApplication.class, args);
    }
}
