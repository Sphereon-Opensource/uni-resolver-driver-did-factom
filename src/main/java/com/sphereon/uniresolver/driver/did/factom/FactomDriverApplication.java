package com.sphereon.uniresolver.driver.did.factom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.sphereon.uniresolver", "org.factomprotocol.identity.did"})
public class FactomDriverApplication {
    public static void main(String[] args) {
        SpringApplication.run(FactomDriverApplication.class, args);
    }
}
