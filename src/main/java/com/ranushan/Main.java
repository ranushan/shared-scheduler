package com.ranushan;

import com.ranushan.configuration.ConfigurationHolder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public BatchManager batchManager() {
        var bm = new BatchManager.BatchRunner()
                .configurationHolder(new ConfigurationHolder())
                .scanPackage("com.ranushan")
                .build();
        bm.startAllBatches();
        return bm;
    }
}