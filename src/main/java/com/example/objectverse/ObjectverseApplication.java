package com.example.objectverse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ObjectverseApplication {

    private static final Logger log =
            LoggerFactory.getLogger(ObjectverseApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ObjectverseApplication.class, args);
        log.info("ObjectVerse started successfully.");
    }

}