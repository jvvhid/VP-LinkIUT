package edu.iutdhaka.linkiut;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LinkIutApplication {

    public static void main(String[] args) {
        SpringApplication.run(LinkIutApplication.class, args);
    }
}
