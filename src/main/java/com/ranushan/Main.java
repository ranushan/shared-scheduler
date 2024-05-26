package com.ranushan;

import com.ranushan.configuration.ConfigurationHolder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Main implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        new BatchManager.BatchRunner()
                .configurationHolder(new ConfigurationHolder())
                .scanPackage("com.ranushan")
                .build()
                .startAllBatches();
    }
}