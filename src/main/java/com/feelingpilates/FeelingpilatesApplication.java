package com.feelingpilates;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FeelingpilatesApplication {

   public static void main(String[] args) {
        // spring-dotenv 4.0.0 se registra vía META-INF/spring.factories, un
        // mecanismo que Spring Boot 4.x ya no lee para SpringApplicationRunListener,
        // así que nunca llegaba a cargar el .env. Lo hacemos manualmente con
        // dotenv-java (dependencia transitiva de spring-dotenv) antes de arrancar
        // el contexto, para que application.properties pueda resolver ${VAR:} igual.
        Dotenv.configure().ignoreIfMissing().load().entries().forEach(entry -> {
            if (System.getProperty(entry.getKey()) == null && System.getenv(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });

        SpringApplication.run(FeelingpilatesApplication.class, args);
    }
}
