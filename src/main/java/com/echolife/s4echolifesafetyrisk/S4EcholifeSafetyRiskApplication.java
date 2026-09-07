package com.echolife.s4echolifesafetyrisk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class S4EcholifeSafetyRiskApplication {

    public static void main(String[] args) {
        SpringApplication.run(S4EcholifeSafetyRiskApplication.class, args);
    }

}