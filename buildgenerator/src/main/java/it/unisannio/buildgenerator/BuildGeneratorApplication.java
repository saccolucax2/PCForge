package it.unisannio.buildgenerator;

import it.unisannio.buildgenerator.presentation.PCBuildController;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication

public class BuildGeneratorApplication extends ResourceConfig {

    public BuildGeneratorApplication() {
        register(PCBuildController.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(BuildGeneratorApplication.class, args);
    }

}