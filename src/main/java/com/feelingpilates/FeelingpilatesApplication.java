package com.feelingpilates;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class FeelingpilatesApplication {

    private static final Logger log = LoggerFactory.getLogger(FeelingpilatesApplication.class);

   public static void main(String[] args) {
        String pass = System.getenv("DB_PASSWORD");
        System.out.println("DEBUG >> DB_HOST=" + System.getenv("DB_HOST"));
        System.out.println("DEBUG >> DB_USER=" + System.getenv("DB_USER"));
        System.out.println("DEBUG >> DB_PASSWORD length=" + (pass != null ? pass.length() : -1));
        if (pass != null && pass.length() > 0) {
            System.out.println("DEBUG >> DB_PASSWORD first/last=" + pass.charAt(0) + "/" + pass.charAt(pass.length()-1));
        }
        SpringApplication.run(FeelingpilatesApplication.class, args);
    }
    
    @PostConstruct
    public void debugEnv() {
        String host = System.getenv("DB_HOST");
        String user = System.getenv("DB_USER");
        String pass = System.getenv("DB_PASSWORD");
        log.info("DEBUG >> DB_HOST={}", host);
        log.info("DEBUG >> DB_USER={}", user);
        log.info("DEBUG >> DB_PASSWORD length={}", pass != null ? pass.length() : -1);
        if (pass != null && pass.length() > 0) {
            log.info("DEBUG >> DB_PASSWORD first/last char={}/{}",
                pass.charAt(0), pass.charAt(pass.length() - 1));
        }
    }
}
